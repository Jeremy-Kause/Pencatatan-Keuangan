package com.app.uangku.model;

public enum SavingsGoalSource {
    BALANCE("BALANCE", "Saldo Saat Ini"),
    MONTHLY_SURPLUS("MONTHLY_SURPLUS", "Surplus Bulan Ini");

    private final String databaseValue;
    private final String displayName;

    SavingsGoalSource(String databaseValue, String displayName) {
        this.databaseValue = databaseValue;
        this.displayName = displayName;
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
