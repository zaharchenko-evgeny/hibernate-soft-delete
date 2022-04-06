package io.github.zaharchenko.hibernate.softdelete.core;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.engine.spi.ExecuteUpdateResultCheckStyle;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.mapping.*;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.hibernate.type.Type;
import io.github.zaharchenko.hibernate.softdelete.core.api.SoftDelete;

import javax.persistence.ManyToMany;
import java.util.Iterator;
import java.util.logging.Logger;

import static java.lang.String.format;

public class SoftDeleteHibernateMetadataIntegrator implements Integrator {

    private Logger log = Logger.getLogger(SoftDeleteHibernateMetadataIntegrator.class.getName());

    private static final String AND_IS_NOT_DELETED_CONDITION = "(%s) AND %s";
    private static final String SOFT_DELETION_SQL_CONDITION = "UPDATE %s SET %s WHERE %s = ?";

    @Override
    public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
        applySoftDeletion(metadata, new SoftDeleteConditions(serviceRegistry));
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {

    }

    private void applySoftDeletion(Metadata metadata, SoftDeleteConditions softDeleteConditions) {
        for (PersistentClass entityBinding : metadata.getEntityBindings()) {
            if (isSoftDeletable(entityBinding)) {
                applySoftDeletion(metadata, entityBinding, softDeleteConditions);
                if (entityBinding instanceof RootClass) {
                    applyWhere((RootClass) entityBinding, softDeleteConditions);
                } else {
                    log.warning(String.format("@Where condition was not applied to inherited entity %s",entityBinding.getClassName()));
                }
            }
            applySoftDeletionFilterForLinkedEntities(metadata, entityBinding, softDeleteConditions);
        }
    }

    private void applySoftDeletionFilterForLinkedEntities(Metadata metadata, PersistentClass entityBinding, SoftDeleteConditions softDeleteConditions) {
        for (Iterator it = entityBinding.getPropertyIterator(); it.hasNext(); ) {
            Property property = (Property) it.next();
            Type propertyType = property.getType();
            if (propertyType.isCollectionType() && property.getValue() instanceof Bag) {
                Bag bag = (Bag) property.getValue();
                PersistentClass propertyClass = getPropertyClass(bag.getElement(), metadata);
                if (propertyClass != null && isSoftDeletable(propertyClass)) {
                    String where = softDeleteConditions.getSoftDeleteWhere(propertyClass);
                    if (where != null) {
                        if (isManyToMany(property)) {
                            applyIgnoreNotFound(bag);
                            addManyToManyWhereCondition(bag, where);
                        } else {
                            addWhereCondition(bag, where);
                        }
                    }
                }

            }
            if (propertyType.isAssociationType() && !propertyType.isCollectionType()) {
                PersistentClass propertyClass = getPropertyClass(property.getValue(), metadata);
                if (propertyClass != null && isSoftDeletable(propertyClass) && property.getValue() instanceof ManyToOne) {
                    ManyToOne manyToOne = (ManyToOne) property.getValue();
                    manyToOne.setIgnoreNotFound(true);
                }
            }
        }
    }

    private PersistentClass getPropertyClass(Value element, Metadata metadata) {
        if (element != null) {
            if (element instanceof OneToMany) {
                return ((OneToMany) element).getAssociatedClass();
            }
            if (element instanceof ManyToOne) {
                return metadata.getEntityBinding(((ManyToOne) element).getReferencedEntityName());
            }
        }
        return null;
    }


    private void applyIgnoreNotFound(Bag bag) {
        if (bag.getElement() instanceof ManyToOne) {
            ManyToOne manyToOne = (ManyToOne) bag.getElement();
            manyToOne.setIgnoreNotFound(true);
        }
    }

    private void addWhereCondition(Filterable bag, String where) {
        if (bag instanceof Collection) {
            Collection collection = (Collection) bag;
            if (collection.getWhere() != null) {
                collection.setWhere(format(
                        AND_IS_NOT_DELETED_CONDITION,
                        collection.getWhere(), where));
            } else {
                collection.setWhere(where);
            }
        }
    }

    private void addManyToManyWhereCondition(Filterable bag, String where) {
        if (bag instanceof Collection) {
            Collection collection = (Collection) bag;
            if (collection.getManyToManyWhere() != null) {
                collection.setManyToManyWhere(format(
                        AND_IS_NOT_DELETED_CONDITION,
                        collection.getManyToManyWhere(), where));
            } else {
                collection.setManyToManyWhere(where);
            }
        }
    }

    private boolean isManyToMany(Property field) {
        try {
            return field.getPersistentClass().getMappedClass().getDeclaredField(field.getName()).getAnnotation(ManyToMany.class) != null;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    private void applySoftDeletion(Metadata metadata, PersistentClass entityBinding, SoftDeleteConditions softDeleteConditions) {
        addSoftDeletedColumn(metadata, entityBinding, softDeleteConditions);
        applySQLDelete(entityBinding, softDeleteConditions);
    }

    private void addSoftDeletedColumn(Metadata metadata, PersistentClass entityBinding, SoftDeleteConditions softDeleteConditions) {
        SoftDelete softDelete = (SoftDelete) entityBinding.getMappedClass().getAnnotation(SoftDelete.class);
        if (!softDelete.property().isEmpty()) {
            if (entityBinding.hasProperty(softDelete.property())) {
                return;
            } else {
                throw new IllegalStateException(
                        String.format("Soft delete property %s not found in entity %s",
                                softDelete.property(), entityBinding.getMappedClass().getName()));
            }
        }

        if (!softDelete.column().isEmpty()) {
            Column column = new Column();
            column.setName(softDelete.column());
            entityBinding.getTable().addColumn(column);

            SimpleValue value = new SimpleValue((MetadataImplementor) metadata, entityBinding.getTable());
            value.setTable(entityBinding.getTable());
            value.setTypeName(softDelete.type().getTypeName());
            value.addColumn(column);

            Property prop = new Property();
            prop.setValue(value);
            prop.setPersistentClass(entityBinding);
            prop.setName(softDelete.column());
            prop.setPropertyAccessorName("noop");
            entityBinding.addProperty(prop);
        }
    }

    private void applySQLDelete(PersistentClass entityBinding, SoftDeleteConditions softDeleteConditions) {
        if (entityBinding.getCustomSQLDelete() == null) {
            String tableName = entityBinding.getTable().getName();
            String softDeleteSetter = softDeleteConditions.getSQLDeleteSetter(entityBinding);
            String idColumn = findIdColumn(entityBinding);
            String customSQLDelete = String.format(SOFT_DELETION_SQL_CONDITION, tableName, softDeleteSetter, idColumn);
            entityBinding.setCustomSQLDelete(customSQLDelete, false, ExecuteUpdateResultCheckStyle.NONE);
        } else {
            log.info(String.format("Soft deletable entity %s already has @SQLDelete annotation", entityBinding.getEntityName()));
        }
    }

    private String findIdColumn(PersistentClass entityBinding) {
        Column column = (Column) entityBinding.getIdentifierProperty().getColumnIterator().next();
        return column.getName();
    }

    private void applyWhere(RootClass entityBinding, SoftDeleteConditions softDeleteConditions) {
        String where = softDeleteConditions.getSoftDeleteWhere(entityBinding);
        if (entityBinding.getWhere() != null) {
            entityBinding.setWhere(format(
                    AND_IS_NOT_DELETED_CONDITION,
                    entityBinding.getWhere(), where)
            );
        } else {
            entityBinding.setWhere(where);
        }
    }

    private boolean isSoftDeletable(PersistentClass entityClass) {
        return entityClass.getMappedClass().getAnnotation(SoftDelete.class) != null;
    }
}
