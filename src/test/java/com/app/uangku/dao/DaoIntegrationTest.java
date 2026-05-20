package com.app.uangku.dao;

import com.app.uangku.model.Budget;
import com.app.uangku.model.Transaction;
import com.app.uangku.model.TransactionType;
import com.app.uangku.model.User;
import com.app.uangku.util.DatabaseHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DaoIntegrationTest {
    @TempDir
    Path tempDir;

    @Test
    void daoFlowWorksWithSqlite() throws Exception {
        Path databasePath = tempDir.resolve("uangku-test.db");
        String databaseUrl = "jdbc:sqlite:" + databasePath.toAbsolutePath().toString().replace("\\", "/");
        DatabaseHelper.setDatabaseUrl(databaseUrl);

        try {
            UserDAO userDAO = new UserDAO();
            CategoryDAO categoryDAO = new CategoryDAO();
            TransactionDAO transactionDAO = new TransactionDAO();
            BudgetDAO budgetDAO = new BudgetDAO();

            User user = userDAO.register("runtime_user", "runtime@example.com", "secret123");
            var categories = categoryDAO.createDefaultCategoriesForUser(user.getIdUser());
            var incomeCategory = categories.stream()
                    .filter(category -> category.getType() == TransactionType.PEMASUKAN)
                    .findFirst()
                    .orElseThrow();
            var expenseCategory = categories.stream()
                    .filter(category -> category.getType() == TransactionType.PENGELUARAN)
                    .findFirst()
                    .orElseThrow();

            transactionDAO.create(new Transaction(
                    user.getIdUser(),
                    incomeCategory.getIdCategory(),
                    100_000,
                    LocalDate.now(),
                    "Runtime income",
                    TransactionType.PEMASUKAN
            ));
            transactionDAO.create(new Transaction(
                    user.getIdUser(),
                    expenseCategory.getIdCategory(),
                    25_000,
                    LocalDate.now(),
                    "Runtime expense",
                    TransactionType.PENGELUARAN
            ));

            budgetDAO.setBudget(new Budget(
                    user.getIdUser(),
                    expenseCategory.getIdCategory(),
                    50_000,
                    YearMonth.now()
            ));

            assertTrue(userDAO.login("runtime_user", "secret123").isPresent());
            assertEquals(75_000, transactionDAO.getBalance(user.getIdUser()));
            assertEquals(1, budgetDAO.findByUserIdAndMonth(user.getIdUser(), YearMonth.now()).size());
        } finally {
            DatabaseHelper.resetDatabaseUrl();
        }
    }
}
