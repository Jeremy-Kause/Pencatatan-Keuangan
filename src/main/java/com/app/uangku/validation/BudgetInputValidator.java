package com.app.uangku.validation;

import com.app.uangku.model.Category;
import com.app.uangku.model.TransactionType;

public final class BudgetInputValidator {
    public ValidationResult validate(Category category) {
        if (category == null) {
            return ValidationResult.failure("Pilih kategori pengeluaran.");
        }
        if (category.getType() != TransactionType.PENGELUARAN) {
            return ValidationResult.failure("Anggaran hanya bisa dibuat untuk kategori pengeluaran.");
        }
        return ValidationResult.success();
    }
}
