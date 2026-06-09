package com.app.uangku.validation;


import java.time.LocalDate;

public final class SavingsGoalInputValidator {
    public ValidationResult validate(String name, double targetAmount, double currentAmount, LocalDate targetDate) {
        if (isBlank(name)) {
            return ValidationResult.failure("Nama target wajib diisi.");
        }
        if (name.trim().length() < 3) {
            return ValidationResult.failure("Nama target minimal 3 karakter.");
        }
        if (name.trim().length() > 50) {
            return ValidationResult.failure("Nama target maksimal 50 karakter.");
        }
        if (targetAmount <= 0) {
            return ValidationResult.failure("Target nominal harus lebih dari 0.");
        }
        if (currentAmount < 0) {
            return ValidationResult.failure("Saldo awal tidak boleh negatif.");
        }
        if (targetDate != null && targetDate.isBefore(LocalDate.now())) {
            return ValidationResult.failure("Tanggal target tidak boleh di masa lalu.");
        }
        return ValidationResult.success();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
