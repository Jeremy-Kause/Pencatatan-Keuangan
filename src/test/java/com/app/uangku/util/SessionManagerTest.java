package com.app.uangku.util;

import com.app.uangku.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionManagerTest {
    @AfterEach
    void tearDown() {
        SessionManager.clear();
    }

    @Test
    void setCurrentUser_marksSessionAsLoggedIn() {
        User user = new User(7, "waraney", "hashed", "waraney@example.com");

        SessionManager.setCurrentUser(user);

        assertTrue(SessionManager.isLoggedIn());
        assertTrue(SessionManager.getCurrentUser().isPresent());
        assertEquals("waraney", SessionManager.getCurrentUser().orElseThrow().getUsername());
    }

    @Test
    void clear_resetsCurrentSession() {
        SessionManager.setCurrentUser(new User(1, "demo", "hash", "demo@example.com"));

        SessionManager.clear();

        assertFalse(SessionManager.isLoggedIn());
        assertTrue(SessionManager.getCurrentUser().isEmpty());
    }
}
