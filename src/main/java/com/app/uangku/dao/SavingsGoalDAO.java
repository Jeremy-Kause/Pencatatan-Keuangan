package com.app.uangku.dao;

import com.app.uangku.model.SavingsGoal;
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
import java.util.List;
import java.util.Optional;

public class SavingsGoalDAO {

    public SavingsGoal create(SavingsGoal goal) throws SQLException {
        String sql = """
                INSERT INTO savings_goals (id_user, name, target_amount, current_amount, target_date, description)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindGoal(statement, goal);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    goal.setIdGoal(keys.getInt(1));
                }
            }
        }

        return findById(goal.getIdGoal(), goal.getIdUser()).orElse(goal);
    }

    public boolean update(SavingsGoal goal) throws SQLException {
        String sql = """
                UPDATE savings_goals
                SET name = ?, target_amount = ?, current_amount = ?, target_date = ?, description = ?
                WHERE id_goal = ? AND id_user = ?
                """;

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, goal.getName());
            statement.setDouble(2, goal.getTargetAmount());
            statement.setDouble(3, goal.getCurrentAmount());
            if (goal.getTargetDate() == null) {
                statement.setNull(4, java.sql.Types.VARCHAR);
            } else {
                statement.setString(4, goal.getTargetDate().toString());
            }
            statement.setString(5, goal.getDescription());
            statement.setInt(6, goal.getIdGoal());
            statement.setInt(7, goal.getIdUser());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteById(int idGoal, int idUser) throws SQLException {
        String sql = "DELETE FROM savings_goals WHERE id_goal = ? AND id_user = ?";

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idGoal);
            statement.setInt(2, idUser);
            return statement.executeUpdate() > 0;
        }
    }

    public List<SavingsGoal> findByUserId(int idUser) throws SQLException {
        String sql = """
                SELECT id_goal, id_user, name, target_amount, current_amount, target_date, description, created_at
                FROM savings_goals
                WHERE id_user = ?
                ORDER BY created_at DESC, id_goal DESC
                """;

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idUser);
            return findMany(statement);
        }
    }

    public Optional<SavingsGoal> findById(int idGoal, int idUser) throws SQLException {
        String sql = """
                SELECT id_goal, id_user, name, target_amount, current_amount, target_date, description, created_at
                FROM savings_goals
                WHERE id_goal = ? AND id_user = ?
                LIMIT 1
                """;

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idGoal);
            statement.setInt(2, idUser);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapGoal(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    private void bindGoal(PreparedStatement statement, SavingsGoal goal) throws SQLException {
        statement.setInt(1, goal.getIdUser());
        statement.setString(2, goal.getName());
        statement.setDouble(3, goal.getTargetAmount());
        statement.setDouble(4, goal.getCurrentAmount());
        if (goal.getTargetDate() == null) {
            statement.setNull(5, java.sql.Types.VARCHAR);
        } else {
            statement.setString(5, goal.getTargetDate().toString());
        }
        statement.setString(6, goal.getDescription());
    }

    private List<SavingsGoal> findMany(PreparedStatement statement) throws SQLException {
        List<SavingsGoal> goals = new ArrayList<>();
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                goals.add(mapGoal(resultSet));
            }
        }
        return goals;
    }

    private SavingsGoal mapGoal(ResultSet resultSet) throws SQLException {
        String targetDate = resultSet.getString("target_date");
        return new SavingsGoal(
                resultSet.getInt("id_goal"),
                resultSet.getInt("id_user"),
                resultSet.getString("name"),
                resultSet.getDouble("target_amount"),
                targetDate == null || targetDate.isBlank() ? null : LocalDate.parse(targetDate),
                resultSet.getString("description"),
                resultSet.getString("created_at"),
                resultSet.getDouble("current_amount")
        );
    }
}
