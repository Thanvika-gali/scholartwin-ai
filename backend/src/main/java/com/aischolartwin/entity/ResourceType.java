package com.aischolartwin.entity;

/**
 * Discriminator enum identifying the specific type of an academic knowledge resource.
 */
public enum ResourceType {
    DOCUMENT,
    NOTE,
    LINK;

    public static ResourceType fromString(String value) {
        if (value == null) return null;
        for (ResourceType type : values()) {
            if (type.name().equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        return null;
    }
}

