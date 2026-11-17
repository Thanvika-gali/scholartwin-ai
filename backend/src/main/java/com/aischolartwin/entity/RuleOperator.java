package com.aischolartwin.entity;

public enum RuleOperator {
    CONTAINS,
    EQUALS,
    STARTS_WITH,
    REGEX;

    public static RuleOperator fromString(String value) {
        if (value == null) return null;
        for (RuleOperator op : values()) {
            if (op.name().equalsIgnoreCase(value.trim())) {
                return op;
            }
        }
        return null;
    }
}

