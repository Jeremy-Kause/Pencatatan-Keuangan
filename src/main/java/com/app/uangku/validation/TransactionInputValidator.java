package com.app.uangku.validation;

import com.app.uangku.model.Category;
import com.app.uangku.model.TransactionType;

import java.time.LocalDate;

public final class TransactionInputValidator {
    public ValidationResult validate(TransactionType type, Category category, LocalDate date, String description) {
        if (type == null) {
            return ValidationResult.failure("Pilih tipe transaksi.");
        }
        if (category == null) {
            return ValidationResult.failure("Pilih kategori transaksi.");
        }
        if (date == null) {
            return ValidationResult.failure("Pilih tanggal transaksi.");
        }
        if (category.getType() != type) {
            return ValidationResult.failure("Kategori harus sesuai dengan tipe transaksi yang dipilih.");
        }
        if (description != null && description.trim().length() > 255) {
            return ValidationResult.failure("Deskripsi maksimal 255 karakter.");
        }
        return ValidationResult.success();
    }

    public ValidationResult validateFilterRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            return ValidationResult.failure("Tanggal mulai tidak boleh melebihi tanggal akhir.");
        }
        return ValidationResult.success();
    }
}
