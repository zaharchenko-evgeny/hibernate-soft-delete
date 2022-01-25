package ru.zaharchenko.hibernate.softdelete.core.conditions;

import org.hibernate.mapping.Column;

public interface SoftDeleteCondition {

    String sqlWhere(Column column, boolean nullable);

    String sqlDeleteSetter(Column column, boolean nullable);
}
