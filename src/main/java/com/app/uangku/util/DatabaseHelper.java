package com.app.uangku.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseHelper {
    private static final String DEFAULT_DATABASE_URL = "jdbc:sqlite:uangku.db";
    private static String databaseUrl = DEFAULT_DATABASE_URL;
    private static boolean initialized;

    private DatabaseHelper() {
    }

    public static Connection getConnection() throws SQLException {
        initializeDatabase();
        return openConnection();
    }

    public static synchronized void initializeDatabase() {
        if (initialized) {
            return;
        }

        try (Connection connection = openConnection();
             Statement statement = connection.createStatement()) {
            for (String query : createTableQueries()) {
                statement.executeUpdate(query);
            }
            initialized = true;
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to initialize SQLite database", exception);
        }
    }

    public static String getDatabaseUrl() {
        return databaseUrl;
    }

    public static synchronized void setDatabaseUrl(String databaseUrl) {
        DatabaseHelper.databaseUrl = databaseUrl;
        initialized = false;
    }

    public static synchronized void resetDatabaseUrl() {
        databaseUrl = DEFAULT_DATABASE_URL;
        initialized = false;
    }

    private static Connection openConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(databaseUrl);
        enableForeignKeys(connection);
        return connection;
    }

    private static void enableForeignKeys(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
    }

    private static String[] createTableQueries() {
        return new String[] {
                """
                CREATE TABLE IF NOT EXISTS users (
                    id_user INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    email TEXT NOT NULL UNIQUE
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS categories (
                    id_category INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_user INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    type TEXT NOT NULL CHECK (type IN ('PEMASUKAN', 'PENGELUARAN')),
                    FOREIGN KEY (id_user) REFERENCES users(id_user) ON DELETE CASCADE,
                    UNIQUE (id_user, name, type),
                    UNIQUE (id_category, id_user),
                    UNIQUE (id_category, id_user, type)
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS transactions (
                    id_transaction INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_user INTEGER NOT NULL,
                    id_category INTEGER NOT NULL,
                    amount REAL NOT NULL CHECK (amount > 0),
                    date TEXT NOT NULL,
                    description TEXT,
                    type TEXT NOT NULL CHECK (type IN ('PEMASUKAN', 'PENGELUARAN')),
                    FOREIGN KEY (id_user) REFERENCES users(id_user) ON DELETE CASCADE,
                    FOREIGN KEY (id_category, id_user, type)
                        REFERENCES categories(id_category, id_user, type)
                        ON DELETE RESTRICT
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS budgets (
                    id_budget INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_user INTEGER NOT NULL,
                    id_category INTEGER NOT NULL,
                    limit_amount REAL NOT NULL CHECK (limit_amount >= 0),
                    month_year TEXT NOT NULL,
                    FOREIGN KEY (id_user) REFERENCES users(id_user) ON DELETE CASCADE,
                    FOREIGN KEY (id_category, id_user)
                        REFERENCES categories(id_category, id_user)
                        ON DELETE CASCADE,
                    UNIQUE (id_user, id_category, month_year)
                )
                """,
                "CREATE INDEX IF NOT EXISTS idx_categories_user_type ON categories(id_user, type)",
                "CREATE INDEX IF NOT EXISTS idx_transactions_user_date ON transactions(id_user, date)",
                "CREATE INDEX IF NOT EXISTS idx_transactions_user_type ON transactions(id_user, type)",
                "CREATE INDEX IF NOT EXISTS idx_transactions_category ON transactions(id_category)",
                "CREATE INDEX IF NOT EXISTS idx_budgets_user_month ON budgets(id_user, month_year)"
        };
    }
}
