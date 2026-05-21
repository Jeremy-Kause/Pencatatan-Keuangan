package com.app.uangku.model;

public enum TransactionType {
    PEMASUKAN("PEMASUKKAN", "Pemasukan"),
    PENGELUARAN("PENGELUARAN", "Pengeluaran");

    private final String databaseValue;
    private final String displayName;

    TransactionType(String databaseValue, String displayName) {
        this.databaseValue = databaseValue;
        this.displayName = displayName;
    }

    public String toDatabaseValue() {
        return databaseValue;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static TransactionType fromDatabaseValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Tipe transaksi tidak boleh kosong");
        }

        String normalized = value.trim().toUpperCase();
        if ("PEMASUKKAN".equals(normalized) || "PEMASUKAN".equals(normalized)) {
            return PEMASUKAN;
        }
        if ("PENGELUARAN".equals(normalized)) {
            return PENGELUARAN;
        }

        throw new IllegalArgumentException("Tipe transaksi tidak dikenal: " + value);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
