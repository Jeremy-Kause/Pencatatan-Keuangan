package com.app.uangku.model;

public enum TransactionType {
    PEMASUKAN,
    PENGELUARAN;

    public String toDatabaseValue() {
        return name();
    }

    public static TransactionType fromDatabaseValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Transaction type cannot be empty");
        }

        return TransactionType.valueOf(value.trim().toUpperCase());
    }
}
