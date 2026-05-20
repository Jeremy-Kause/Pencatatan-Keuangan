package com.app.uangku.dao;

import com.app.uangku.model.Category;
import com.app.uangku.model.TransactionType;
import com.app.uangku.util.DatabaseHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    public Category create(Category category) throws SQLException {
        String sql = """
                INSERT INTO categories (id_user, name, type)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, category.getIdUser());
            statement.setString(2, category.getName());
            statement.setString(3, category.getType().toDatabaseValue());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    category.setIdCategory(keys.getInt(1));
                }
            }
        }

        return category;
    }

    public void createDefaultCategoriesForUser(int idUser) throws SQLException {
        String[] incomes = {"Gaji", "Bonus", "Hadiah", "Lainnya"};
        String[] expenses = {"Makanan", "Transportasi", "Belanja", "Tagihan", "Hiburan", "Lainnya"};

        try (Connection connection = DatabaseHelper.getConnection()) {
            for (String name : incomes) {
                insertIfMissing(connection, idUser, name, TransactionType.PEMASUKAN);
            }
            for (String name : expenses) {
                insertIfMissing(connection, idUser, name, TransactionType.PENGELUARAN);
            }
        }
    }

    public List<Category> findByUserId(int idUser) throws SQLException {
        String sql = """
                SELECT id_category, id_user, name, type
                FROM categories
                WHERE id_user = ?
                ORDER BY type, name COLLATE NOCASE
                """;

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idUser);
            return findMany(statement);
        }
    }

    public List<Category> findByUserIdAndType(int idUser, TransactionType type) throws SQLException {
        String sql = """
                SELECT id_category, id_user, name, type
                FROM categories
                WHERE id_user = ? AND type = ?
                ORDER BY name COLLATE NOCASE
                """;

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idUser);
            statement.setString(2, type.toDatabaseValue());
            return findMany(statement);
        }
    }

    public boolean update(Category category) throws SQLException {
        String sql = """
                UPDATE categories
                SET name = ?, type = ?
                WHERE id_category = ? AND id_user = ?
                """;

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, category.getName());
            statement.setString(2, category.getType().toDatabaseValue());
            statement.setInt(3, category.getIdCategory());
            statement.setInt(4, category.getIdUser());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteById(int idCategory, int idUser) throws SQLException {
        String sql = "DELETE FROM categories WHERE id_category = ? AND id_user = ?";

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idCategory);
            statement.setInt(2, idUser);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean isUsedByTransaction(int idCategory, int idUser) throws SQLException {
        String sql = """
                SELECT 1
                FROM transactions
                WHERE id_category = ? AND id_user = ?
                LIMIT 1
                """;

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idCategory);
            statement.setInt(2, idUser);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public boolean existsByUserIdAndTypeAndName(int idUser, TransactionType type, String name) throws SQLException {
        String sql = """
                SELECT 1
                FROM categories
                WHERE id_user = ? AND type = ? AND LOWER(name) = LOWER(?)
                LIMIT 1
                """;

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idUser);
            statement.setString(2, type.toDatabaseValue());
            statement.setString(3, name.trim());

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public boolean existsByUserIdAndTypeAndNameExceptId(
            int idUser,
            TransactionType type,
            String name,
            int excludedIdCategory
    ) throws SQLException {
        String sql = """
                SELECT 1
                FROM categories
                WHERE id_user = ? AND type = ? AND LOWER(name) = LOWER(?) AND id_category <> ?
                LIMIT 1
                """;

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idUser);
            statement.setString(2, type.toDatabaseValue());
            statement.setString(3, name.trim());
            statement.setInt(4, excludedIdCategory);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void insertIfMissing(
            Connection connection,
            int idUser,
            String name,
            TransactionType type
    ) throws SQLException {
        String checkSql = """
                SELECT 1
                FROM categories
                WHERE id_user = ? AND LOWER(name) = LOWER(?) AND type = ?
                LIMIT 1
                """;
        try (PreparedStatement check = connection.prepareStatement(checkSql)) {
            check.setInt(1, idUser);
            check.setString(2, name);
            check.setString(3, type.toDatabaseValue());

            try (ResultSet resultSet = check.executeQuery()) {
                if (resultSet.next()) {
                    return;
                }
            }
        }

        try (PreparedStatement insert = connection.prepareStatement("""
                INSERT INTO categories (id_user, name, type)
                VALUES (?, ?, ?)
                """)) {
            insert.setInt(1, idUser);
            insert.setString(2, name);
            insert.setString(3, type.toDatabaseValue());
            insert.executeUpdate();
        }
    }

    private List<Category> findMany(PreparedStatement statement) throws SQLException {
        List<Category> categories = new ArrayList<>();

        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                categories.add(mapCategory(resultSet));
            }
        }

        return categories;
    }

    private Category mapCategory(ResultSet resultSet) throws SQLException {
        return new Category(
                resultSet.getInt("id_category"),
                resultSet.getInt("id_user"),
                resultSet.getString("name"),
                TransactionType.fromDatabaseValue(resultSet.getString("type"))
        );
    }
}
