package ru.zaharchenko.hibernate.softdelete.core;

import org.hibernate.mapping.*;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import ru.zaharchenko.hibernate.softdelete.core.api.SoftDelete;
import ru.zaharchenko.hibernate.softdelete.core.api.SoftDeleteColumn;
import ru.zaharchenko.hibernate.softdelete.core.conditions.*;

import java.util.AbstractMap;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.String.format;

public class SoftDeleteConditions {

    protected Map<Class, SoftDeleteCondition> conditionMap = new HashMap<>();

    public SoftDeleteConditions(SessionFactoryServiceRegistry serviceRegistry) {
        conditionMap.put(Boolean.class, new BooleanCondition());
        conditionMap.put(Number.class, new NumberCondition());
        conditionMap.put(Date.class, new DateCondition());
        conditionMap.put(String.class, new StringCondition());
    }

    private String softDeletionPropertyOperation(PersistentClass entityBinding, SoftDeletionOperation operation) {
        Map.Entry<Column, Class> deleteProperty = findSoftDeletedProperty(entityBinding);
        if (deleteProperty == null) {
            return null;
        }
        Class propertyClass = deleteProperty.getValue();
        Column column = deleteProperty.getKey();

        Class key = conditionMap.containsKey(propertyClass)
                ? propertyClass
                : conditionMap.keySet().stream().filter(clazz -> clazz.isAssignableFrom(propertyClass)).findFirst().orElse(null);
        if (key == null) {
            throw new IllegalStateException(format(
                    "Unsupported soft deletion property '%s' class %s (class: %s)",
                    deleteProperty.getKey(),
                    propertyClass,
                    entityBinding.getClassName())
            );
        }

        SoftDeleteCondition condition = conditionMap.get(key);

        if (column!=null){
            return operation.apply(condition, column, column.isNullable());
        } else {
            throw new IllegalStateException(format(
                    "Soft deletion property should be mapped to column (class: %s)",
                    entityBinding.getClassName())
            );
        }
    }

    public String getSoftDeleteWhere(PersistentClass entityBinding) {
        return softDeletionPropertyOperation(entityBinding, SoftDeleteCondition::sqlWhere);
    }

    public String getSQLDeleteSetter(PersistentClass entityBinding) {
        return softDeletionPropertyOperation(entityBinding, SoftDeleteCondition::sqlDeleteSetter);
    }

    private Map.Entry<Column, Class> findSoftDeletedProperty(PersistentClass persistentClass) {
        SoftDelete softDelete = (SoftDelete) persistentClass.getMappedClass().getAnnotation(SoftDelete.class);
        if (!softDelete.property().isEmpty()) {
            return new AbstractMap.SimpleEntry<>(findPropertyColumn(persistentClass, softDelete.property()), softDelete.type());
        }
        if (!softDelete.column().isEmpty()) {
            //TODO check column exists
            return new AbstractMap.SimpleEntry<>(findPropertyColumn(persistentClass,softDelete.column()), softDelete.type());
        }
        return Stream.of(persistentClass.getMappedClass().getDeclaredFields())
                .filter(f -> f.getAnnotation(SoftDeleteColumn.class) != null)
                .findFirst()
                .map(f -> new AbstractMap.SimpleEntry<Column, Class>(findPropertyColumn(persistentClass, f.getName()), f.getType()))
                .orElse(null);
    }

    private Column findPropertyColumn(PersistentClass persistentClass, String propertyName) {
        Property property = persistentClass.getProperty(propertyName);
        Value value = property.getValue();
        if (value instanceof SimpleValue) {
            SimpleValue simpleValue = (SimpleValue) value;
            if (simpleValue.getColumnIterator().hasNext()) {
                Column column = (Column) simpleValue.getColumnIterator().next();
                return column;
            }
        } else {
            throw new IllegalStateException(format(
                    "Soft deletion property '%s' should have primitive type (class: %s)",
                    property.getName(),
                    persistentClass.getClassName())
            );
        }
        return null;
    }

    @FunctionalInterface
    private interface SoftDeletionOperation {
        String apply(SoftDeleteCondition condition, Column column, boolean isNullable);
    }

}
