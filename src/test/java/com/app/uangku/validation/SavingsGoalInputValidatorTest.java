package com.app.uangku.validation;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SavingsGoalInputValidatorTest {
    private final SavingsGoalInputValidator validator = new SavingsGoalInputValidator();

    @Test
    void validate_acceptsValidInput() {
        ValidationResult result = validator.validate(
                "Laptop",
                8_000_000,
                2_000_000,
                LocalDate.now().plusMonths(3)
        );

        assertTrue(result.isValid());
    }

    @Test
    void validate_rejectsNegativeTargetAmount() {
        ValidationResult result = validator.validate(
                "Laptop",
                -1,
                0,
                LocalDate.now().plusMonths(3)
        );

        assertFalse(result.isValid());
    }

    @Test
    void validate_rejectsPastTargetDate() {
        ValidationResult result = validator.validate(
                "Laptop",
                8_000_000,
                0,
                LocalDate.now().minusDays(1)
        );

        assertFalse(result.isValid());
    }
}
