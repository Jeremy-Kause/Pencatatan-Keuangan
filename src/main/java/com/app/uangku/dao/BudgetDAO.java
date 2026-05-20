package com.app.uangku.dao;

import com.app.uangku.model.Budget;
import com.app.uangku.model.TransactionType;
import com.app.uangku.util.DatabaseHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BudgetDAO {
    public Budget setBudget(Budget budget) throws SQLException {
        String sql = """
                INSERT INTO budgets (id_user, id_category, limit_amount, month_year)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(id_user, id_category, month_year)
                DO UPDATE SET limit_amount = excluded.limit_amount
                """;

        try (Connection connection = DatabaseHelper.getConnection()) {
            validateExpenseCategory(connection, budget.getIdUser(), budget.getIdCategory());

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, budget.getIdUser());
                statement.setInt(2, budget.getIdCategory());
                statement.setDouble(3, budget.getLimitAmount());
                statement.setString(4, budget.getMonthYear().toString());
                statement.executeUpdate();
            }
        }

        return findByCategoryAndMonth(
                budget.getIdUser(),
                budget.getIdCategory(),
                budget.getMonthYear()
        ).orElse(budget);
    }

    public Optional<Budget> findById(int idBudget, int idUser) throws SQLException {
        String sql = selectBudgetWithUsage() + """
                WHERE b.id_budget = ? AND b.id_user = ?
                """;

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idBudget);
            statement.setInt(2, idUser);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapBudget(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    public Optional<Budget> findByCategoryAndMonth(
            int idUser,
            int idCategory,
            YearMonth monthYear
    ) throws SQLException {
        String sql = selectBudgetWithUsage() + """
                WHERE b.id_user = ?
                    AND b.id_category = ?
                    AND b.month_year = ?
                """;

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idUser);
            statement.setInt(2, idCategory);
            statement.setString(3, monthYear.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapBudget(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    public List<Budget> findByUserIdAndMonth(int idUser, YearMonth monthYear) throws SQLException {
        String sql = selectBudgetWithUsage() + """
                WHERE b.id_user = ?
                    AND b.month_year = ?
                ORDER BY c.name COLLATE NOCASE
                """;

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idUser);
            statement.setString(2, monthYear.toString());
            return findMany(statement);
        }
    }

    public boolean update(Budget budget) throws SQLException {
        String sql = """
                UPDATE budgets
                SET id_category = ?, limit_amount = ?, month_year = ?
                WHERE id_budget = ? AND id_user = ?
                """;

        try (Connection connection = DatabaseHelper.getConnection()) {
            validateExpenseCategory(connection, budget.getIdUser(), budget.getIdCategory());

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, budget.getIdCategory());
                statement.setDouble(2, budget.getLimitAmount());
                statement.setString(3, budget.getMonthYear().toString());
                statement.setInt(4, budget.getIdBudget());
                statement.setInt(5, budget.getIdUser());
                return statement.executeUpdate() > 0;
            }
        }
    }

    public boolean deleteById(int idBudget, int idUser) throws SQLException {
        String sql = "DELETE FROM budgets WHERE id_budget = ? AND id_user = ?";

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idBudget);
            statement.setInt(2, idUser);
            return statement.executeUpdate() > 0;
        }
    }

    public double getUsedAmount(int idUser, int idCategory, YearMonth monthYear) throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(amount), 0) AS used_amount
                FROM transactions
                WHERE id_user = ?
                    AND id_category = ?
                    AND type = ?
                    AND date BETWEEN ? AND ?
                """;

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idUser);
            statement.setInt(2, idCategory);
            statement.setString(3, TransactionType.PENGELUARAN.toDatabaseValue());
            statement.setString(4, monthYear.atDay(1).toString());
            statement.setString(5, monthYear.atEndOfMonth().toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("used_amount");
                }
            }
        }

        return 0;
    }

    private void validateExpenseCategory(Connection connection, int idUser, int idCategory) throws SQLException {
        String sql = """
                SELECT 1
                FROM categories
                WHERE id_user = ?
                    AND id_category = ?
                    AND type = ?
                LIMIT 1
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idUser);
            statement.setInt(2, idCategory);
            statement.setString(3, TransactionType.PENGELUARAN.toDatabaseValue());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Budget must use a PENGELUARAN category owned by the user");
                }
            }
        }
    }

    private String selectBudgetWithUsage() {
        return """
                SELECT
                    b.id_budget,
                    b.id_user,
                    b.id_category,
                    b.limit_amount,
                    b.month_year,
                    c.name AS category_name,
                    COALESCE((
                        SELECT SUM(t.amount)
                        FROM transactions t
                        WHERE t.id_user = b.id_user
                            AND t.id_category = b.id_category
                            AND t.type = 'PENGELUARAN'
                            AND t.date BETWEEN b.month_year || '-01'
                                AND date(b.month_year || '-01', '+1 month', '-1 day')
                    ), 0) AS used_amount
                FROM budgets b
                JOIN categories c
                    ON c.id_category = b.id_category
                    AND c.id_user = b.id_user
                """;
    }

    private List<Budget> findMany(PreparedStatement statement) throws SQLException {
        List<Budget> budgets = new ArrayList<>();

        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                budgets.add(mapBudget(resultSet));
            }
        }

        return budgets;
    }

    private Budget mapBudget(ResultSet resultSet) throws SQLException {
        return new Budget(
                resultSet.getInt("id_budget"),
                resultSet.getInt("id_user"),
                resultSet.getInt("id_category"),
                resultSet.getDouble("limit_amount"),
                YearMonth.parse(resultSet.getString("month_year")),
                resultSet.getString("category_name"),
                resultSet.getDouble("used_amount")
        );
    }
}
