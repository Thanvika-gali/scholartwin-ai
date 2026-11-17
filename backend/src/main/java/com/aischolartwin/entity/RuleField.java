package com.aischolartwin.entity;

public enum RuleField {
    FILENAME,
    TITLE,
    CONTENT,
    FILE_TYPE;

    public static RuleField fromString(String value) {
        if (value == null) return null;
        for (RuleField f : values()) {
            if (f.name().equalsIgnoreCase(value.trim())) {
                return f;
            }
        }
        return null;
    }
}

