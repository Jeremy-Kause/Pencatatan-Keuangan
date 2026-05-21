package com.app.uangku.validation;

import com.app.uangku.model.TransactionType;

public final class CategoryInputValidator {
    public ValidationResult validate(String name, TransactionType type) {
        if (isBlank(name)) {
            return ValidationResult.failure("Nama kategori wajib diisi.");
        }
        if (name.length() < 2) {
            return ValidationResult.failure("Nama kategori minimal 2 karakter.");
        }
        if (name.length() > 30) {
            return ValidationResult.failure("Nama kategori maksimal 30 karakter.");
        }
        if (type == null) {
            return ValidationResult.failure("Pilih tipe kategori.");
        }
        return ValidationResult.success();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
