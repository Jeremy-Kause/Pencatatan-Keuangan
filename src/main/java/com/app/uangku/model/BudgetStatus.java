package com.app.uangku.model;

public class BudgetStatus {
    public static final BudgetStatus AMAN = new BudgetStatus("Aman");
    public static final BudgetStatus MENDEKATI_LIMIT = new BudgetStatus("Mendekati limit");
    public static final BudgetStatus MELEBIHI_LIMIT = new BudgetStatus("Melebihi limit");

    private final String displayName;

    private BudgetStatus(String displayName) {
        this.displayName = displayName;
    }

    public static BudgetStatus[] values() {
        return new BudgetStatus[]{AMAN, MENDEKATI_LIMIT, MELEBIHI_LIMIT};
    }

    public String getDisplayName() {
        return displayName;
    }
}
