package com.app.uangku.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthInputValidatorTest {
    private final AuthInputValidator validator = new AuthInputValidator();

    @Test
    void loginValidation_acceptsFilledCredentials() {
        ValidationResult result = validator.validateLogin("waraney", "secret123");

        assertTrue(result.isValid());
    }

    @Test
    void loginValidation_rejectsBlankCredentials() {
        ValidationResult result = validator.validateLogin("", "");

        assertFalse(result.isValid());
    }

    @Test
    void registrationValidation_acceptsValidInput() {
        ValidationResult result = validator.validateRegistration("waraney", "waraney@example.com", "secret123");

        assertTrue(result.isValid());
    }

    @Test
    void registrationValidation_rejectsInvalidUsername() {
        ValidationResult result = validator.validateRegistration("wa", "waraney@example.com", "secret123");

        assertFalse(result.isValid());
    }

    @Test
    void registrationValidation_rejectsInvalidEmail() {
        ValidationResult result = validator.validateRegistration("waraney", "waraneyexample.com", "secret123");

        assertFalse(result.isValid());
    }

    @Test
    void registrationValidation_rejectsShortPassword() {
        ValidationResult result = validator.validateRegistration("waraney", "waraney@example.com", "123");

        assertFalse(result.isValid());
    }
}
