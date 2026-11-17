package com.aischolartwin.entity;

public enum CareerSkillCategory {
    PROGRAMMING_LANGUAGES("Programming Languages"),
    FRONTEND("Frontend Development"),
    BACKEND("Backend Development"),
    DATABASES("Databases & Storage"),
    CLOUD_DEVOPS("Cloud & DevOps"),
    AI_ML("AI & Machine Learning"),
    TESTING("Testing & QA"),
    TOOLS("Developer Tools"),
    CORE_CS("Core Computer Science"),
    OTHER("Other Technical Skills");

    private final String displayName;

    CareerSkillCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

