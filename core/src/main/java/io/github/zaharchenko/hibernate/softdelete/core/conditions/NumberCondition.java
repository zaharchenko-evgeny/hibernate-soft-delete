package io.github.zaharchenko.hibernate.softdelete.core.conditions;

import org.hibernate.mapping.Column;

public class NumberCondition implements SoftDeleteCondition {

    @Override
    public String sqlWhere(Column column, boolean nullable) {
        if (nullable) return String.format("%s is null", column.getName());
        else return String.format("%s = 0", column.getName());
    }

    @Override
    public String sqlDeleteSetter(Column column, boolean nullable) {
        return String.format("%s = 1", column.getName());
    }
}
