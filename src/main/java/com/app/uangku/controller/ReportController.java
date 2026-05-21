package com.app.uangku.controller;

import com.app.uangku.dao.TransactionDAO;
import com.app.uangku.model.Transaction;
import com.app.uangku.model.User;
import com.app.uangku.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.sql.SQLException;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public class ReportController extends BaseWireframeController {
    private final TransactionDAO transactionDAO = new TransactionDAO();

    @FXML
    private ComboBox<YearMonth> reportMonthComboBox;

    @FXML
    private PieChart expensePieChart;

    @FXML
    private Label reportTotalExpenseLabel;

    @FXML
    private Label topCategoryLabel;

    @FXML
    private Label topCategoryPercentLabel;

    @FXML
    private Label transactionCountLabel;

    @FXML
    private Label reportNoteLabel;

    @FXML
    private void initialize() {
        YearMonth current = YearMonth.now();
        reportMonthComboBox.setItems(FXCollections.observableArrayList(
                current,
                current.minusMonths(1),
                current.minusMonths(2),
                current.minusMonths(3),
                current.minusMonths(4),
                current.minusMonths(5)
        ));
        reportMonthComboBox.setValue(current);
        reportMonthComboBox.valueProperty().addListener((observable, oldValue, newValue) -> loadReport());
        loadReport();
    }

    private void loadReport() {
        if (!hasActiveSession()) {
            expensePieChart.setData(FXCollections.emptyObservableList());
            setMessage(reportTotalExpenseLabel, formatCurrency(0));
            setMessage(topCategoryLabel, "Belum ada data");
            setMessage(topCategoryPercentLabel, "Login diperlukan");
            setMessage(transactionCountLabel, "0 transaksi");
            setErrorMessage(reportNoteLabel, "Silakan login untuk melihat laporan.");
            return;
        }

        try {
            User user = SessionManager.getCurrentUser().orElseThrow();
            YearMonth month = reportMonthComboBox.getValue();
            Map<String, Double> expenseByCategory = transactionDAO.getExpenseByCategory(user.getIdUser(), month);
            double totalExpense = expenseByCategory.values().stream().mapToDouble(Double::doubleValue).sum();

            expensePieChart.setData(FXCollections.observableArrayList(
                    expenseByCategory.entrySet().stream()
                            .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                            .toList()
            ));
            setMessage(reportTotalExpenseLabel, formatCurrency(totalExpense));
            setTopCategory(expenseByCategory, totalExpense);

            List<Transaction> transactions = transactionDAO.filter(
                    user.getIdUser(),
                    month.atDay(1),
                    month.atEndOfMonth(),
                    null,
                    null,
                    null
            );
            setMessage(transactionCountLabel, transactions.size() + " transaksi");
            clearMessage(reportNoteLabel);
            setMessage(reportNoteLabel, "Laporan bulan " + formatMonth(month));
        } catch (SQLException exception) {
            setErrorMessage(reportNoteLabel, "Gagal memuat laporan: " + exception.getMessage());
        }
    }

    private void setTopCategory(Map<String, Double> expenseByCategory, double totalExpense) {
        if (expenseByCategory.isEmpty() || totalExpense <= 0) {
            setMessage(topCategoryLabel, "Belum ada pengeluaran");
            setMessage(topCategoryPercentLabel, "0% dari total pengeluaran");
            return;
        }

        Map.Entry<String, Double> top = expenseByCategory.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow();
        double percentage = (top.getValue() / totalExpense) * 100;
        setMessage(topCategoryLabel, top.getKey());
        setMessage(topCategoryPercentLabel, String.format("%.1f%% dari total pengeluaran", percentage));
    }
}
