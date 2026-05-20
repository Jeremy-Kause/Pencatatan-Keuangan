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
        return create(new User(username, plainPassword, email));
    }

    public User create(User user) throws SQLException {
        String sql = """
                INSERT INTO users (username, password, email)
                VALUES (?, ?, ?)
                """;
        String passwordHash = PasswordUtil.hashPassword(user.getPassword());

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, passwordHash);
            statement.setString(3, user.getEmail());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setIdUser(generatedKeys.getInt(1));
                }
            }

            user.setPassword(passwordHash);
            return user;
        }
    }

    public Optional<User> login(String usernameOrEmail, String plainPassword) throws SQLException {
        Optional<User> user = findByUsernameOrEmail(usernameOrEmail);
        if (user.isPresent() && PasswordUtil.verifyPassword(plainPassword, user.get().getPassword())) {
            return user;
        }

        return Optional.empty();
    }

    public Optional<User> findById(int idUser) throws SQLException {
        String sql = """
                SELECT id_user, username, password, email
                FROM users
                WHERE id_user = ?
                """;

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idUser);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = """
                SELECT id_user, username, password, email
                FROM users
                WHERE username = ?
                """;

        return findOneByValue(sql, username);
    }

    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = """
                SELECT id_user, username, password, email
                FROM users
                WHERE email = ?
                """;

        return findOneByValue(sql, email);
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
        String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";
        return exists(sql, username);
    }

    public boolean isEmailTaken(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";
        return exists(sql, email);
    }

    public boolean updateProfile(User user) throws SQLException {
        String sql = """
                UPDATE users
                SET username = ?, email = ?
                WHERE id_user = ?
                """;

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setInt(3, user.getIdUser());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean updatePassword(int idUser, String plainPassword) throws SQLException {
        String sql = """
                UPDATE users
                SET password = ?
                WHERE id_user = ?
                """;

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, PasswordUtil.hashPassword(plainPassword));
            statement.setInt(2, idUser);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteById(int idUser) throws SQLException {
        String sql = "DELETE FROM users WHERE id_user = ?";

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idUser);
            return statement.executeUpdate() > 0;
        }
    }

    private Optional<User> findOneByValue(String sql, String value) throws SQLException {
        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
            }
        }

        return Optional.empty();
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
