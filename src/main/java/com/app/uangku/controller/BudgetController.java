package com.app.uangku.controller;

import com.app.uangku.dao.BudgetDAO;
import com.app.uangku.dao.CategoryDAO;
import com.app.uangku.model.Budget;
import com.app.uangku.model.Category;
import com.app.uangku.model.TransactionType;
import com.app.uangku.model.User;
import com.app.uangku.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.YearMonth;
import java.util.List;

public class BudgetController extends BaseWireframeController {
    private final BudgetDAO budgetDAO = new BudgetDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @FXML
    private ComboBox<Category> budgetCategoryComboBox;

    @FXML
    private TextField budgetMonthField;

    @FXML
    private TextField budgetLimitField;

    @FXML
    private GridPane budgetCardsGrid;

    @FXML
    private TableView<Budget> budgetTable;

    @FXML
    private Label budgetMessageLabel;

    @FXML
    private void initialize() {
        budgetMonthField.setText(YearMonth.now().toString());
        setupTable();
        loadCategories();
        refreshBudgets();
    }

    @FXML
    private void handleSaveBudget() {
        if (!hasActiveSession()) {
            setMessage(budgetMessageLabel, "Silakan login terlebih dahulu.");
            return;
        }

        try {
            Category category = budgetCategoryComboBox.getValue();
            setMessage(budgetMessageLabel, "");
            if (category == null) {
                setMessage(budgetMessageLabel, "Pilih kategori pengeluaran.");
                return;
            }
            if (category.getType() != TransactionType.PENGELUARAN) {
                setMessage(budgetMessageLabel, "Anggaran hanya bisa dibuat untuk kategori pengeluaran.");
                return;
            }

            User user = SessionManager.getCurrentUser().orElseThrow();
            budgetDAO.setBudget(new Budget(
                    user.getIdUser(),
                    category.getIdCategory(),
                    parseAmount(budgetLimitField.getText()),
                    parseMonth(budgetMonthField.getText())
            ));

            budgetLimitField.clear();
            refreshBudgets();
            setMessage(budgetMessageLabel, "Anggaran berhasil disimpan.");
        } catch (NumberFormatException exception) {
            setMessage(budgetMessageLabel, "Nominal anggaran tidak valid.");
        } catch (IllegalArgumentException | SQLException exception) {
            setMessage(budgetMessageLabel, exception.getMessage());
        }
    }

    @FXML
    private void focusBudgetForm() {
        budgetCategoryComboBox.requestFocus();
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {
        if (budgetTable == null || budgetTable.getColumns().isEmpty()) {
            return;
        }

        TableColumn<Budget, String> categoryColumn = (TableColumn<Budget, String>) budgetTable.getColumns().get(0);
        TableColumn<Budget, String> monthColumn = (TableColumn<Budget, String>) budgetTable.getColumns().get(1);
        TableColumn<Budget, String> limitColumn = (TableColumn<Budget, String>) budgetTable.getColumns().get(2);
        TableColumn<Budget, String> usedColumn = (TableColumn<Budget, String>) budgetTable.getColumns().get(3);
        TableColumn<Budget, String> statusColumn = (TableColumn<Budget, String>) budgetTable.getColumns().get(4);

        categoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategoryName()));
        monthColumn.setCellValueFactory(data -> new SimpleStringProperty(formatMonth(data.getValue().getMonthYear())));
        limitColumn.setCellValueFactory(data -> new SimpleStringProperty(formatCurrency(data.getValue().getLimitAmount())));
        usedColumn.setCellValueFactory(data -> new SimpleStringProperty(formatCurrency(data.getValue().getUsedAmount())));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().getDisplayName()));
    }

    private void loadCategories() {
        if (!hasActiveSession()) {
            return;
        }

        try {
            User user = SessionManager.getCurrentUser().orElseThrow();
            List<Category> categories = categoryDAO.findByUserIdAndType(user.getIdUser(), TransactionType.PENGELUARAN);
            budgetCategoryComboBox.setItems(FXCollections.observableArrayList(categories));
            if (categories.isEmpty()) {
                setMessage(budgetMessageLabel, "Tambahkan kategori pengeluaran terlebih dahulu.");
            }
        } catch (SQLException exception) {
            setMessage(budgetMessageLabel, "Gagal memuat kategori: " + exception.getMessage());
        }
    }

    private void refreshBudgets() {
        if (!hasActiveSession() || budgetTable == null) {
            return;
        }

        try {
            User user = SessionManager.getCurrentUser().orElseThrow();
            YearMonth month = parseMonth(budgetMonthField.getText());
            List<Budget> budgets = budgetDAO.findByUserIdAndMonth(user.getIdUser(), month);
            budgetTable.setItems(FXCollections.observableArrayList(budgets));
            renderBudgetCards(budgets);
        } catch (IllegalArgumentException | SQLException exception) {
            setMessage(budgetMessageLabel, "Gagal memuat anggaran: " + exception.getMessage());
        }
    }

    private void renderBudgetCards(List<Budget> budgets) {
        budgetCardsGrid.getChildren().clear();

        if (budgets.isEmpty()) {
            Label emptyLabel = new Label("Belum ada anggaran untuk bulan ini");
            emptyLabel.getStyleClass().add("muted-text");
            budgetCardsGrid.add(emptyLabel, 0, 0);
            return;
        }

        for (int index = 0; index < budgets.size(); index++) {
            Budget budget = budgets.get(index);
            budgetCardsGrid.add(createBudgetCard(budget), index % 2, index / 2);
        }
    }

    private VBox createBudgetCard(Budget budget) {
        Label limitLabel = new Label(formatCurrency(budget.getLimitAmount()));
        limitLabel.getStyleClass().add("placeholder-title");

        Label categoryLabel = new Label(budget.getCategoryName());
        categoryLabel.getStyleClass().add("muted-text");

        ProgressBar progressBar = new ProgressBar(Math.min(1, budget.getUsagePercentage() / 100));
        progressBar.setMaxWidth(Double.MAX_VALUE);

        Label statusLabel = new Label(budget.getStatus().getDisplayName());
        statusLabel.getStyleClass().add(
                budget.getUsedAmount() > budget.getLimitAmount() ? "danger-text" : "success-text"
        );

        Label usedLabel = new Label("Terpakai " + formatCurrency(budget.getUsedAmount()));
        usedLabel.getStyleClass().add("muted-text");

        VBox card = new VBox(10, limitLabel, categoryLabel, progressBar, usedLabel, statusLabel);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("budget-card");
        return card;
    }
}
