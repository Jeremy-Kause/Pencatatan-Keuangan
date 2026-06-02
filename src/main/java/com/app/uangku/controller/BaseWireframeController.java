package com.app.uangku.controller;

import com.app.uangku.util.SessionManager;
import com.app.uangku.util.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public abstract class BaseWireframeController {
    private static final Locale INDONESIA = new Locale("id", "ID");
    private static final NumberFormat RUPIAH_FORMAT = NumberFormat.getCurrencyInstance(INDONESIA);
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy", INDONESIA);
    private static final Map<String, Integer> MONTHS = Map.ofEntries(
            Map.entry("januari", 1),
            Map.entry("februari", 2),
            Map.entry("maret", 3),
            Map.entry("april", 4),
            Map.entry("mei", 5),
            Map.entry("juni", 6),
            Map.entry("juli", 7),
            Map.entry("agustus", 8),
            Map.entry("september", 9),
            Map.entry("oktober", 10),
            Map.entry("november", 11),
            Map.entry("desember", 12)
    );

    @FXML
    protected void showLogin(ActionEvent event) throws IOException {
        SceneManager.switchTo(event, "login.fxml");
    }

    @FXML
    protected void showRegister(ActionEvent event) throws IOException {
        SceneManager.switchTo(event, "register.fxml");
    }

    @FXML
    protected void showDashboard(ActionEvent event) throws IOException {
        SceneManager.switchTo(event, "dashboard.fxml");
    }

    @FXML
    protected void showTransactions(ActionEvent event) throws IOException {
        SceneManager.switchTo(event, "transactions.fxml");
    }

    @FXML
    protected void showCategories(ActionEvent event) throws IOException {
        SceneManager.switchTo(event, "categories.fxml");
    }

    @FXML
    protected void showBudgets(ActionEvent event) throws IOException {
        SceneManager.switchTo(event, "budgets.fxml");
    }

    @FXML
    protected void showGoals(ActionEvent event) throws IOException {
        SceneManager.switchTo(event, "goals.fxml");
    }

    @FXML
    protected void showReports(ActionEvent event) throws IOException {
        SceneManager.switchTo(event, "reports.fxml");
    }

    @FXML
    protected void logout(ActionEvent event) throws IOException {
        SessionManager.clear();
        showLogin(event);
    }

    protected boolean hasActiveSession() {
        return SessionManager.isLoggedIn();
    }

    protected String formatCurrency(double value) {
        return RUPIAH_FORMAT.format(value).replace(",00", "");
    }

    protected String formatMonth(YearMonth month) {
        return MONTH_FORMAT.format(month);
    }

    protected YearMonth parseMonth(String value) {
        if (value == null || value.isBlank()) {
            return YearMonth.now();
        }

        String trimmed = value.trim();
        try {
            return YearMonth.parse(trimmed);
        } catch (DateTimeParseException ignored) {
            String[] parts = trimmed.toLowerCase(INDONESIA).split("\\s+");
            if (parts.length == 2 && MONTHS.containsKey(parts[0])) {
                return YearMonth.of(Integer.parseInt(parts[1]), MONTHS.get(parts[0]));
            }
            throw new IllegalArgumentException("Format bulan gunakan yyyy-MM, contoh 2026-05");
        }
    }

    protected double parseAmount(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Nominal wajib diisi");
        }

        String normalized = value
                .replace("Rp", "")
                .replace("rp", "")
                .replace(".", "")
                .replace(",", ".")
                .trim();
        double amount = Double.parseDouble(normalized);
        if (amount <= 0) {
            throw new IllegalArgumentException("Nominal harus lebih dari 0");
        }

        return amount;
    }

    protected void setMessage(Label label, String message) {
        if (label != null) {
            label.getStyleClass().removeAll("success-text", "danger-text");
            label.setText(message == null ? "" : message);
        }
    }

    protected void setSuccessMessage(Label label, String message) {
        applyMessageStyle(label, message, "success-text");
    }

    protected void setErrorMessage(Label label, String message) {
        applyMessageStyle(label, message, "danger-text");
    }

    protected void clearMessage(Label label) {
        if (label != null) {
            label.getStyleClass().removeAll("success-text", "danger-text");
            label.setText("");
        }
    }

    private void applyMessageStyle(Label label, String message, String styleClass) {
        if (label == null) {
            return;
        }

        label.getStyleClass().removeAll("success-text", "danger-text");
        if (!label.getStyleClass().contains(styleClass)) {
            label.getStyleClass().add(styleClass);
        }
        label.setText(message == null ? "" : message);
    }

    protected boolean confirmAction(String title, String message, String confirmText) {
        ButtonType confirmButton = new ButtonType(confirmText, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, confirmButton, cancelButton);
        alert.setTitle(title);
        alert.setHeaderText(null);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == confirmButton;
    }
}
