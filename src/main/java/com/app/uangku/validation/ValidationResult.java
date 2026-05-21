package com.app.uangku.validation;

public final class ValidationResult {
    private static final ValidationResult SUCCESS = new ValidationResult(true, "");

    private final boolean valid;
    private final String message;

    private ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message == null ? "" : message;
    }

    public static ValidationResult success() {
        return SUCCESS;
    }

    public static ValidationResult failure(String message) {
        return new ValidationResult(false, message);
    }

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }
}
