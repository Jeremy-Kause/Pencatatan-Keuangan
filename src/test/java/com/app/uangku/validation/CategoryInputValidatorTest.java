package com.app.uangku.validation;

import com.app.uangku.model.TransactionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoryInputValidatorTest {
    private final CategoryInputValidator validator = new CategoryInputValidator();

    @Test
    void validate_acceptsValidCategory() {
        ValidationResult result = validator.validate("Makan", TransactionType.PENGELUARAN);

        assertTrue(result.isValid());
    }

    @Test
    void validate_rejectsBlankName() {
        ValidationResult result = validator.validate("", TransactionType.PENGELUARAN);

        assertFalse(result.isValid());
    }

    @Test
    void validate_rejectsTooShortName() {
        ValidationResult result = validator.validate("A", TransactionType.PEMASUKAN);

        assertFalse(result.isValid());
    }

    @Test
    void validate_rejectsTooLongName() {
        ValidationResult result = validator.validate("KategoriDenganNamaYangTerlaluPanjangSekali", TransactionType.PEMASUKAN);

        assertFalse(result.isValid());
    }

    @Test
    void validate_rejectsNullType() {
        ValidationResult result = validator.validate("Gaji", null);

        assertFalse(result.isValid());
    }
}
