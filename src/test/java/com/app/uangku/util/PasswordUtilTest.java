package com.app.uangku.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {
    @Test
    void hashPassword_producesStructuredHash() {
        String hash = PasswordUtil.hashPassword("secret123");

        assertNotNull(hash);
        assertTrue(hash.startsWith("pbkdf2_sha256$"));
        assertFalse(hash.contains("secret123"));
    }

    @Test
    void verifyPassword_acceptsCorrectPasswordAndRejectsWrongPassword() {
        String hash = PasswordUtil.hashPassword("secret123");

        assertTrue(PasswordUtil.verifyPassword("secret123", hash));
        assertFalse(PasswordUtil.verifyPassword("wrong-password", hash));
    }

    @Test
    void verifyPassword_rejectsMalformedOrNullStoredPassword() {
        assertFalse(PasswordUtil.verifyPassword("secret123", null));
        assertFalse(PasswordUtil.verifyPassword(null, "pbkdf2_sha256$120000$salt$hash"));
        assertFalse(PasswordUtil.verifyPassword("secret123", "invalid-format"));
    }

    @Test
    void hashPassword_usesRandomSalt() {
        String firstHash = PasswordUtil.hashPassword("secret123");
        String secondHash = PasswordUtil.hashPassword("secret123");

        assertNotEquals(firstHash, secondHash);
    }
}
