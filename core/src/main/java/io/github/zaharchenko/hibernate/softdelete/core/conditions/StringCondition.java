package io.github.zaharchenko.hibernate.softdelete.core.conditions;

import org.hibernate.mapping.Column;

public class StringCondition implements SoftDeleteCondition {

    private static final String DELETED_MARKER = "deleted";

    @Override
    public String sqlWhere(Column column, boolean nullable) {
        if (nullable) return String.format("%s is null", column.getName());
        else return String.format("%s = ''", column.getName());
    }

    @Override
    public String sqlDeleteSetter(Column column, boolean nullable) {
        return String.format("%s = '%s'", column.getName(), DELETED_MARKER.substring(0, Math.min(DELETED_MARKER.length(), column.getLength())));
    }
}
