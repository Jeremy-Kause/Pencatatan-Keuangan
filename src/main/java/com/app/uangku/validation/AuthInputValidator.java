package com.app.uangku.validation;

import java.util.regex.Pattern;

public final class AuthInputValidator {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9._]{3,20}$");

    public ValidationResult validateLogin(String usernameOrEmail, String password) {
        if (isBlank(usernameOrEmail) || isBlank(password)) {
            return ValidationResult.failure("Username/email dan password wajib diisi.");
        }
        return ValidationResult.success();
    }

    public ValidationResult validateRegistration(String username, String email, String password) {
        if (isBlank(username) || isBlank(email) || isBlank(password)) {
            return ValidationResult.failure("Semua field wajib diisi.");
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return ValidationResult.failure("Username 3-20 karakter dan hanya boleh huruf, angka, titik, atau underscore.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ValidationResult.failure("Format email belum valid.");
        }
        if (password.length() < 6) {
            return ValidationResult.failure("Password minimal 6 karakter.");
        }
        return ValidationResult.success();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
