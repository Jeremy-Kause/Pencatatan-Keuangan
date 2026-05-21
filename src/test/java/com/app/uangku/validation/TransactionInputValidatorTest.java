package com.app.uangku.validation;

import com.app.uangku.model.Category;
import com.app.uangku.model.TransactionType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionInputValidatorTest {
    private final TransactionInputValidator validator = new TransactionInputValidator();

    @Test
    void validate_acceptsConsistentTransactionInput() {
        Category category = new Category(1, 1, "Makan", TransactionType.PENGELUARAN);

        ValidationResult result = validator.validate(
                TransactionType.PENGELUARAN,
                category,
                LocalDate.of(2026, 5, 22),
                "Makan siang"
        );

        assertTrue(result.isValid());
    }

    @Test
    void validate_rejectsMissingType() {
        Category category = new Category(1, 1, "Makan", TransactionType.PENGELUARAN);

        ValidationResult result = validator.validate(
                null,
                category,
                LocalDate.of(2026, 5, 22),
                "Makan siang"
        );

        assertFalse(result.isValid());
    }

    @Test
    void validate_rejectsMismatchedTypeAndCategory() {
        Category category = new Category(1, 1, "Gaji", TransactionType.PEMASUKAN);

        ValidationResult result = validator.validate(
                TransactionType.PENGELUARAN,
                category,
                LocalDate.of(2026, 5, 22),
                "Salah tipe"
        );

        assertFalse(result.isValid());
    }

    @Test
    void validate_rejectsLongDescription() {
        Category category = new Category(1, 1, "Makan", TransactionType.PENGELUARAN);
        String description = "x".repeat(256);

        ValidationResult result = validator.validate(
                TransactionType.PENGELUARAN,
                category,
                LocalDate.of(2026, 5, 22),
                description
        );

        assertFalse(result.isValid());
    }

    @Test
    void validateFilterRange_acceptsChronologicalRange() {
        ValidationResult result = validator.validateFilterRange(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 22)
        );

        assertTrue(result.isValid());
    }

    @Test
    void validateFilterRange_rejectsInvertedRange() {
        ValidationResult result = validator.validateFilterRange(
                LocalDate.of(2026, 5, 22),
                LocalDate.of(2026, 5, 1)
        );

        assertFalse(result.isValid());
    }
}
