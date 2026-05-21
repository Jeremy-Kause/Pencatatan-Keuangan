package com.app.uangku.controller;

import com.app.uangku.dao.BudgetDAO;
import com.app.uangku.dao.TransactionDAO;
import com.app.uangku.model.Budget;
import com.app.uangku.model.Transaction;
import com.app.uangku.model.User;
import com.app.uangku.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.SQLException;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

public class DashboardController extends BaseWireframeController {
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final BudgetDAO budgetDAO = new BudgetDAO();

    @FXML
    private Label incomeSummaryLabel;

    @FXML
    private Label balanceSummaryLabel;

    @FXML
    private Label expenseSummaryLabel;

    @FXML
    private TableView<Transaction> recentTransactionsTable;

    @FXML
    private Label budgetSummaryLabel;

    @FXML
    private Label topExpenseCategoryLabel;

    @FXML
    private Label reminderLabel;

    @FXML
    private void initialize() {
        setupTable();
        loadDashboard();
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {
        if (recentTransactionsTable == null || recentTransactionsTable.getColumns().isEmpty()) {
            return;
        }

        TableColumn<Transaction, String> dateColumn = (TableColumn<Transaction, String>) recentTransactionsTable.getColumns().get(0);
        TableColumn<Transaction, String> categoryColumn = (TableColumn<Transaction, String>) recentTransactionsTable.getColumns().get(1);
        TableColumn<Transaction, String> typeColumn = (TableColumn<Transaction, String>) recentTransactionsTable.getColumns().get(2);
        TableColumn<Transaction, String> amountColumn = (TableColumn<Transaction, String>) recentTransactionsTable.getColumns().get(3);

        dateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate().toString()));
        categoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategoryName()));
        typeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType().getDisplayName()));
        amountColumn.setCellValueFactory(data -> new SimpleStringProperty(formatCurrency(data.getValue().getAmount())));
    }

    private void loadDashboard() {
        if (!hasActiveSession()) {
            setEmptyState();
            return;
        }

        User user = SessionManager.getCurrentUser().orElseThrow();
        YearMonth month = YearMonth.now();

        try {
            incomeSummaryLabel.setText(formatCurrency(transactionDAO.getTotalIncome(user.getIdUser(), month)));
            expenseSummaryLabel.setText(formatCurrency(transactionDAO.getTotalExpense(user.getIdUser(), month)));
            balanceSummaryLabel.setText(formatCurrency(transactionDAO.getBalance(user.getIdUser())));
            recentTransactionsTable.setItems(FXCollections.observableArrayList(
                    transactionDAO.findRecentByUserId(user.getIdUser(), 10)
            ));
            loadSideSummary(user, month);
        } catch (SQLException exception) {
            setEmptyState();
            setMessage(reminderLabel, "Gagal memuat dashboard: " + exception.getMessage());
        }
    }

    private void loadSideSummary(User user, YearMonth month) throws SQLException {
        List<Budget> budgets = budgetDAO.findByUserIdAndMonth(user.getIdUser(), month);
        if (budgets.isEmpty()) {
            setMessage(budgetSummaryLabel, "Belum ada anggaran bulan ini");
        } else {
            long overLimit = budgets.stream()
                    .filter(budget -> budget.getUsedAmount() > budget.getLimitAmount())
                    .count();
            setMessage(budgetSummaryLabel, overLimit + " anggaran melewati limit dari " + budgets.size());
        }

        var expenseByCategory = transactionDAO.getExpenseByCategory(user.getIdUser(), month);
        String topCategory = expenseByCategory.entrySet().stream()
                .max(Comparator.comparingDouble(entry -> entry.getValue()))
                .map(entry -> entry.getKey() + " - " + formatCurrency(entry.getValue()))
                .orElse("Belum ada pengeluaran");
        setMessage(topExpenseCategoryLabel, topCategory);
        setMessage(reminderLabel, "Data bulan " + formatMonth(month));
    }

    private void setEmptyState() {
        setMessage(incomeSummaryLabel, formatCurrency(0));
        setMessage(expenseSummaryLabel, formatCurrency(0));
        setMessage(balanceSummaryLabel, formatCurrency(0));
        if (recentTransactionsTable != null) {
            recentTransactionsTable.setItems(FXCollections.emptyObservableList());
        }
        setMessage(budgetSummaryLabel, "Belum ada data");
        setMessage(topExpenseCategoryLabel, "Belum ada data");
        setMessage(reminderLabel, "Silakan login terlebih dahulu");
    }
}
