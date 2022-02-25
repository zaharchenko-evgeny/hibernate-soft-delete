package ru.zaharchenko.hibernate.softdelete.core.conditions;

import org.hibernate.mapping.Column;

/**
 * Base interface to apply soft delete conditions
 *
 * @author Evgeny Zakharchenko
 */
public interface SoftDeleteCondition {

    /**
     * SQL injection to determine not deleted records.
     * Will be applied added to SQL in @Where annotation, if @Where annotation exists.
     *
     * @param column Hibernate column mapping
     * @param nullable column is nullable or not
     */
    String sqlWhere(Column column, boolean nullable);

    /**
     * SQL assignment to mark entity deleted
     *
     * @param column Hibernate column mapping
     * @param nullable column is nullable or not
     */
    String sqlDeleteSetter(Column column, boolean nullable);
}
