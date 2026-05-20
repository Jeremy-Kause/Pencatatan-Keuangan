package com.app.uangku.dao;

import com.app.uangku.model.Transaction;
import com.app.uangku.model.TransactionType;
import com.app.uangku.util.DatabaseHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TransactionDAO {
    public Transaction create(Transaction transaction) throws SQLException {
        String sql = """
                INSERT INTO transactions (id_user, id_category, amount, date, description, type)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, transaction.getIdUser());
            statement.setInt(2, transaction.getIdCategory());
            statement.setDouble(3, transaction.getAmount());
            statement.setString(4, transaction.getDate().toString());
            statement.setString(5, transaction.getDescription());
            statement.setString(6, transaction.getType().toDatabaseValue());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    transaction.setIdTransaction(keys.getInt(1));
                }
            }
        }

        return transaction;
    }

    public List<Transaction> findByUserId(int idUser) throws SQLException {
        String sql = selectWithCategory() + """
                WHERE t.id_user = ?
                ORDER BY t.date DESC, t.id_transaction DESC
                """;

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idUser);
            return findMany(statement);
        }
    }

    public List<Transaction> findRecentByUserId(int idUser, int limit) throws SQLException {
        String sql = selectWithCategory() + """
                WHERE t.id_user = ?
                ORDER BY t.date DESC, t.id_transaction DESC
                LIMIT ?
                """;

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idUser);
            statement.setInt(2, limit);
            return findMany(statement);
        }
    }

    public List<Transaction> filter(
            int idUser,
            LocalDate startDate,
            LocalDate endDate,
            TransactionType type,
            Integer idCategory,
            String keyword
    ) throws SQLException {
        StringBuilder sql = new StringBuilder(selectWithCategory());
        List<Object> parameters = new ArrayList<>();

        sql.append("WHERE t.id_user = ? ");
        parameters.add(idUser);

        if (startDate != null) {
            sql.append("AND t.date >= ? ");
            parameters.add(startDate.toString());
        }
        if (endDate != null) {
            sql.append("AND t.date <= ? ");
            parameters.add(endDate.toString());
        }
        if (type != null) {
            sql.append("AND t.type = ? ");
            parameters.add(type.toDatabaseValue());
        }
        if (idCategory != null && idCategory > 0) {
            sql.append("AND t.id_category = ? ");
            parameters.add(idCategory);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND LOWER(COALESCE(t.description, '') || ' ' || c.name) LIKE ? ");
            parameters.add("%" + keyword.trim().toLowerCase() + "%");
        }

        sql.append("ORDER BY t.date DESC, t.id_transaction DESC");

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bind(statement, parameters);
            return findMany(statement);
        }
    }

    public boolean deleteById(int idTransaction, int idUser) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id_transaction = ? AND id_user = ?";

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idTransaction);
            statement.setInt(2, idUser);
            return statement.executeUpdate() > 0;
        }
    }

    public double getTotalAmountByType(
            int idUser,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate
    ) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT COALESCE(SUM(amount), 0) AS total
                FROM transactions
                WHERE id_user = ? AND type = ?
                """);
        List<Object> parameters = new ArrayList<>();
        parameters.add(idUser);
        parameters.add(type.toDatabaseValue());

        if (startDate != null) {
            sql.append("AND date >= ? ");
            parameters.add(startDate.toString());
        }
        if (endDate != null) {
            sql.append("AND date <= ? ");
            parameters.add(endDate.toString());
        }

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bind(statement, parameters);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getDouble("total") : 0;
            }
        }
    }

    public double getTotalIncome(int idUser, YearMonth month) throws SQLException {
        return getTotalAmountByType(idUser, TransactionType.PEMASUKAN, month.atDay(1), month.atEndOfMonth());
    }

    public double getTotalExpense(int idUser, YearMonth month) throws SQLException {
        return getTotalAmountByType(idUser, TransactionType.PENGELUARAN, month.atDay(1), month.atEndOfMonth());
    }

    public double getBalance(int idUser) throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(
                    CASE
                        WHEN type = ? THEN amount
                        ELSE -amount
                    END
                ), 0) AS balance
                FROM transactions
                WHERE id_user = ?
                """;

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, TransactionType.PEMASUKAN.toDatabaseValue());
            statement.setInt(2, idUser);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getDouble("balance") : 0;
            }
        }
    }

    public Map<String, Double> getExpenseByCategory(int idUser, YearMonth month) throws SQLException {
        String sql = """
                SELECT c.name AS category_name, COALESCE(SUM(t.amount), 0) AS total
                FROM transactions t
                JOIN categories c ON c.id_category = t.id_category
                WHERE t.id_user = ?
                    AND t.type = ?
                    AND t.date BETWEEN ? AND ?
                GROUP BY c.id_category, c.name
                ORDER BY total DESC
                """;
        Map<String, Double> data = new LinkedHashMap<>();

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idUser);
            statement.setString(2, TransactionType.PENGELUARAN.toDatabaseValue());
            statement.setString(3, month.atDay(1).toString());
            statement.setString(4, month.atEndOfMonth().toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    data.put(resultSet.getString("category_name"), resultSet.getDouble("total"));
                }
            }
        }

        return data;
    }

    private String selectWithCategory() {
        return """
                SELECT
                    t.id_transaction,
                    t.id_user,
                    t.id_category,
                    t.amount,
                    t.date,
                    t.description,
                    t.type,
                    c.name AS category_name
                FROM transactions t
                JOIN categories c ON c.id_category = t.id_category
                """;
    }

    private List<Transaction> findMany(PreparedStatement statement) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();

        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                transactions.add(mapTransaction(resultSet));
            }
        }

        return transactions;
    }

    private void bind(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            Object value = parameters.get(i);
            int index = i + 1;
            if (value instanceof Integer integer) {
                statement.setInt(index, integer);
            } else if (value instanceof Double number) {
                statement.setDouble(index, number);
            } else {
                statement.setString(index, String.valueOf(value));
            }
        }
    }

    private Transaction mapTransaction(ResultSet resultSet) throws SQLException {
        return new Transaction(
                resultSet.getInt("id_transaction"),
                resultSet.getInt("id_user"),
                resultSet.getInt("id_category"),
                resultSet.getDouble("amount"),
                LocalDate.parse(resultSet.getString("date")),
                resultSet.getString("description"),
                TransactionType.fromDatabaseValue(resultSet.getString("type")),
                resultSet.getString("category_name")
        );
    }
}
