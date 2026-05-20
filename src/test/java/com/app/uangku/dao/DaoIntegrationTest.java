package com.app.uangku.dao;

import com.app.uangku.model.Budget;
import com.app.uangku.model.Transaction;
import com.app.uangku.model.TransactionType;
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
    void daoFlowWorksWithSqliteSchema() throws Exception {
        Path databasePath = tempDir.resolve("uangku-test.db");
        DatabaseHelper.setDatabaseUrl("jdbc:sqlite:" + databasePath.toAbsolutePath().toString().replace("\\", "/"));

        try {
            UserDAO userDAO = new UserDAO();
            CategoryDAO categoryDAO = new CategoryDAO();
            TransactionDAO transactionDAO = new TransactionDAO();
            BudgetDAO budgetDAO = new BudgetDAO();

            var user = userDAO.register("tester", "tester@example.com", "secret123");
            categoryDAO.createDefaultCategoriesForUser(user.getIdUser());

            var incomeCategory = categoryDAO.findByUserIdAndType(user.getIdUser(), TransactionType.PEMASUKAN)
                    .get(0);
            var expenseCategory = categoryDAO.findByUserIdAndType(user.getIdUser(), TransactionType.PENGELUARAN)
                    .get(0);

            transactionDAO.create(new Transaction(
                    user.getIdUser(),
                    incomeCategory.getIdCategory(),
                    100_000,
                    LocalDate.now(),
                    "Gaji",
                    TransactionType.PEMASUKAN
            ));
            transactionDAO.create(new Transaction(
                    user.getIdUser(),
                    expenseCategory.getIdCategory(),
                    25_000,
                    LocalDate.now(),
                    "Makan",
                    TransactionType.PENGELUARAN
            ));
            budgetDAO.setBudget(new Budget(
                    user.getIdUser(),
                    expenseCategory.getIdCategory(),
                    50_000,
                    YearMonth.now()
            ));

            assertTrue(userDAO.login("tester", "secret123").isPresent());
            assertEquals(75_000, transactionDAO.getBalance(user.getIdUser()));
            assertEquals(1, budgetDAO.findByUserIdAndMonth(user.getIdUser(), YearMonth.now()).size());
        } finally {
            DatabaseHelper.resetDatabaseUrl();
        }
    }
}
