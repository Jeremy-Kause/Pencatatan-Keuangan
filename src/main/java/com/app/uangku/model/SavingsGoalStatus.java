package com.app.uangku.model;

public class SavingsGoalStatus {
    public static final SavingsGoalStatus ON_TRACK = new SavingsGoalStatus("On track");
    public static final SavingsGoalStatus NEAR_TARGET = new SavingsGoalStatus("Mendekati target");
    public static final SavingsGoalStatus ACHIEVED = new SavingsGoalStatus("Tercapai");

    private final String displayName;

    private SavingsGoalStatus(String displayName) {
        this.displayName = displayName;
    }

    public static SavingsGoalStatus[] values() {
        return new SavingsGoalStatus[]{ON_TRACK, NEAR_TARGET, ACHIEVED};
    }

    public String getDisplayName() {
        return displayName;
    }
}
