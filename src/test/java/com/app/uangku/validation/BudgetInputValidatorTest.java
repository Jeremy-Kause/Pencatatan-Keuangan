package com.app.uangku.validation;

import com.app.uangku.model.Category;
import com.app.uangku.model.TransactionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BudgetInputValidatorTest {
    private final BudgetInputValidator validator = new BudgetInputValidator();

    @Test
    void validate_acceptsExpenseCategory() {
        Category category = new Category(1, 1, "Makan", TransactionType.PENGELUARAN);

        ValidationResult result = validator.validate(category);

        assertTrue(result.isValid());
    }

    @Test
    void validate_rejectsNullCategory() {
        ValidationResult result = validator.validate(null);

        assertFalse(result.isValid());
    }

    @Test
    void validate_rejectsIncomeCategory() {
        Category category = new Category(1, 1, "Gaji", TransactionType.PEMASUKAN);

        ValidationResult result = validator.validate(category);

        assertFalse(result.isValid());
    }
}
