package com.app.uangku.model;

public enum BudgetStatus {
    AMAN("Aman"),
    MENDEKATI_LIMIT("Mendekati limit"),
    MELEBIHI_LIMIT("Melebihi limit");

    private final String displayName;

    BudgetStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
