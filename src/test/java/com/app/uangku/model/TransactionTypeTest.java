package com.app.uangku.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionTypeTest {
    @Test
    void fromDatabaseValue_acceptsLegacyAndCurrentIncomeValue() {
        assertEquals(TransactionType.PEMASUKAN, TransactionType.fromDatabaseValue("PEMASUKKAN"));
        assertEquals(TransactionType.PEMASUKAN, TransactionType.fromDatabaseValue("PEMASUKAN"));
        assertEquals(TransactionType.PEMASUKAN, TransactionType.fromDatabaseValue(" pemasukan "));
    }

    @Test
    void fromDatabaseValue_acceptsExpenseValue() {
        assertEquals(TransactionType.PENGELUARAN, TransactionType.fromDatabaseValue("PENGELUARAN"));
    }

    @Test
    void fromDatabaseValue_rejectsUnknownOrBlankValue() {
        assertThrows(IllegalArgumentException.class, () -> TransactionType.fromDatabaseValue(""));
        assertThrows(IllegalArgumentException.class, () -> TransactionType.fromDatabaseValue("LAINNYA"));
    }

    @Test
    void displayNameAndStringRepresentation_areConsistent() {
        assertEquals("Pemasukan", TransactionType.PEMASUKAN.getDisplayName());
        assertEquals("Pengeluaran", TransactionType.PENGELUARAN.toString());
    }
}
