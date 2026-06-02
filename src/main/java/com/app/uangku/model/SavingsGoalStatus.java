package com.app.uangku.model;

public enum SavingsGoalStatus {
    ON_TRACK("On track"),
    NEAR_TARGET("Mendekati target"),
    ACHIEVED("Tercapai");

    private final String displayName;

    SavingsGoalStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
