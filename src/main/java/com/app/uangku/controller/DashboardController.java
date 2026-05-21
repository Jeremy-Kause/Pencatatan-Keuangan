package com.app.uangku.controller;

import com.app.uangku.dao.BudgetDAO;
import com.app.uangku.dao.TransactionDAO;
import com.app.uangku.model.Budget;
import com.app.uangku.model.Transaction;
import com.app.uangku.model.TransactionType;
import com.app.uangku.model.User;
import com.app.uangku.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class DashboardController extends BaseWireframeController {
    private static final DateTimeFormatter RECENT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("id", "ID"));

    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final BudgetDAO budgetDAO = new BudgetDAO();

    @FXML
    private Label incomeSummaryLabel;

    @FXML
    private Label balanceSummaryLabel;

    @FXML
    private Label expenseSummaryLabel;

    @FXML
    private VBox recentTransactionsList;

    @FXML
    private Label budgetSummaryLabel;

    @FXML
    private Label topExpenseCategoryLabel;

    @FXML
    private Label reminderLabel;

    @FXML
    private void initialize() {
        loadDashboard();
    }

    private void loadDashboard() {
        if (!hasActiveSession()) {
            setEmptyState();
            return;
        }

        User user = SessionManager.getCurrentUser().orElseThrow();
        YearMonth month = YearMonth.now();

        try {
            incomeSummaryLabel.setText(formatDashboardCurrency(transactionDAO.getTotalIncome(user.getIdUser(), month)));
            expenseSummaryLabel.setText(formatDashboardCurrency(transactionDAO.getTotalExpense(user.getIdUser(), month)));
            balanceSummaryLabel.setText(formatDashboardCurrency(transactionDAO.getBalance(user.getIdUser())));
            renderRecentTransactions(transactionDAO.findRecentByUserId(user.getIdUser(), 4));
            loadSideSummary(user, month);
        } catch (SQLException exception) {
            setEmptyState();
            setErrorMessage(reminderLabel, "Gagal memuat dashboard: " + exception.getMessage());
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

    private void renderRecentTransactions(List<Transaction> transactions) {
        if (recentTransactionsList == null) {
            return;
        }

        recentTransactionsList.getChildren().clear();
        if (transactions.isEmpty()) {
            recentTransactionsList.getChildren().add(createEmptyTransactionCard("Belum ada transaksi terbaru"));
            return;
        }

        for (int index = 0; index < transactions.size(); index++) {
            recentTransactionsList.getChildren().add(createTransactionCard(transactions.get(index), index + 1));
        }
    }

    private HBox createTransactionCard(Transaction transaction, int index) {
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("transaction-card");

        VBox content = new VBox(5);
        content.getStyleClass().add("transaction-card-content");

        Label title = new Label(resolveTransactionTitle(transaction, index));
        title.getStyleClass().add("transaction-title");

        Label subtitle = new Label(formatTransactionSubtitle(transaction));
        subtitle.getStyleClass().add("transaction-subtitle");

        content.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label amount = new Label(formatSignedAmount(transaction));
        amount.getStyleClass().add("transaction-amount");
        amount.getStyleClass().add(transaction.getType() == TransactionType.PEMASUKAN
                ? "transaction-income"
                : "transaction-expense");

        card.getChildren().addAll(content, spacer, amount);
        return card;
    }

    private HBox createEmptyTransactionCard(String message) {
        HBox card = new HBox();
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("transaction-card");

        Label label = new Label(message);
        label.getStyleClass().add("transaction-title");
        card.getChildren().add(label);
        return card;
    }

    private String resolveTransactionTitle(Transaction transaction, int index) {
        if (transaction.getDescription() != null && !transaction.getDescription().isBlank()) {
            return transaction.getDescription().trim();
        }
        if (transaction.getCategoryName() != null && !transaction.getCategoryName().isBlank()) {
            return transaction.getCategoryName();
        }
        return "KONTEN " + index;
    }

    private String formatTransactionSubtitle(Transaction transaction) {
        String category = transaction.getCategoryName() == null || transaction.getCategoryName().isBlank()
                ? "Tanpa kategori"
                : transaction.getCategoryName();
        return category + " - " + RECENT_DATE_FORMAT.format(transaction.getDate()) + " - "
                + transaction.getType().getDisplayName();
    }

    private String formatSignedAmount(Transaction transaction) {
        String sign = transaction.getType() == TransactionType.PEMASUKAN ? "+ " : "- ";
        return sign + formatDashboardCurrency(transaction.getAmount());
    }

    private String formatDashboardCurrency(double value) {
        return "RP " + formatCurrency(value).replace("Rp", "").trim();
    }

    private void setEmptyState() {
        setMessage(incomeSummaryLabel, formatDashboardCurrency(0));
        setMessage(expenseSummaryLabel, formatDashboardCurrency(0));
        setMessage(balanceSummaryLabel, formatDashboardCurrency(0));
        if (recentTransactionsList != null) {
            recentTransactionsList.getChildren().setAll(createEmptyTransactionCard("Belum ada transaksi terbaru"));
        }
        setMessage(budgetSummaryLabel, "Belum ada data");
        setMessage(topExpenseCategoryLabel, "Belum ada data");
        setMessage(reminderLabel, "Silakan login terlebih dahulu");
    }
}
