package com.app.uangku.dao;

import com.app.uangku.model.User;
import com.app.uangku.util.DatabaseHelper;
import com.app.uangku.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class UserDAO {
    public User register(String username, String email, String plainPassword) throws SQLException {
        String sql = """
                INSERT INTO users (username, password, email)
                VALUES (?, ?, ?)
                """;
        User user = new User(username, PasswordUtil.hashPassword(plainPassword), email);

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getEmail());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setIdUser(keys.getInt(1));
                }
            }
        }

        return user;
    }

    public Optional<User> login(String usernameOrEmail, String plainPassword) throws SQLException {
        Optional<User> user = findByUsernameOrEmail(usernameOrEmail);
        if (user.isPresent() && PasswordUtil.verifyPassword(plainPassword, user.get().getPassword())) {
            return user;
        }

        return Optional.empty();
    }

    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) throws SQLException {
        String sql = """
                SELECT id_user, username, password, email
                FROM users
                WHERE username = ? OR email = ?
                LIMIT 1
                """;

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, usernameOrEmail);
            statement.setString(2, usernameOrEmail);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    public boolean isUsernameTaken(String username) throws SQLException {
        return exists("SELECT 1 FROM users WHERE username = ? LIMIT 1", username);
    }

    public boolean isEmailTaken(String email) throws SQLException {
        return exists("SELECT 1 FROM users WHERE email = ? LIMIT 1", email);
    }

    private boolean exists(String sql, String value) throws SQLException {
        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getInt("id_user"),
                resultSet.getString("username"),
                resultSet.getString("password"),
                resultSet.getString("email")
        );
    }
}
