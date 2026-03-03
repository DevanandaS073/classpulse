package com.classpulse.model;

public enum AcademicYear {
    FIRST_YEAR("First Year"),
    SECOND_YEAR("Second Year");

    private final String displayName;

    AcademicYear(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
