package com.app.uangku.model;

public class SavingsGoalSource {
    public static final SavingsGoalSource BALANCE = new SavingsGoalSource("BALANCE", "Saldo Saat Ini");
    public static final SavingsGoalSource MONTHLY_SURPLUS = new SavingsGoalSource("MONTHLY_SURPLUS", "Surplus Bulan Ini");

    private final String databaseValue;
    private final String displayName;

    private SavingsGoalSource(String databaseValue, String displayName) {
        this.databaseValue = databaseValue;
        this.displayName = displayName;
    }

    public static SavingsGoalSource[] values() {
        return new SavingsGoalSource[]{BALANCE, MONTHLY_SURPLUS};
    }

    public String toDatabaseValue() {
        return databaseValue;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static SavingsGoalSource fromDatabaseValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Sumber progres tidak boleh kosong");
        }

        String normalized = value.trim().toUpperCase();
        for (SavingsGoalSource source : values()) {
            if (source.databaseValue.equals(normalized)) {
                return source;
            }
        }

        throw new IllegalArgumentException("Sumber progres tidak dikenal: " + value);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
