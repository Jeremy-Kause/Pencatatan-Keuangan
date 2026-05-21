package com.app.uangku.util;

import com.app.uangku.model.User;

import java.util.Optional;

public final class SessionManager {
    private static User currentUser;

    private SessionManager() {
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void clear() {
        currentUser = null;
    }
}
