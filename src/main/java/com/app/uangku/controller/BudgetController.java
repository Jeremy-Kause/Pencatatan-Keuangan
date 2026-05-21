package com.app.uangku.controller;

import com.app.uangku.dao.BudgetDAO;
import com.app.uangku.dao.CategoryDAO;
import com.app.uangku.model.Budget;
import com.app.uangku.model.BudgetStatus;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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

        budgetTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        categoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategoryName()));
        monthColumn.setCellValueFactory(data -> new SimpleStringProperty(formatMonth(data.getValue().getMonthYear())));
        limitColumn.setCellValueFactory(data -> new SimpleStringProperty(formatCurrency(data.getValue().getLimitAmount())));
        usedColumn.setCellValueFactory(data -> new SimpleStringProperty(formatCurrency(data.getValue().getUsedAmount())));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().getDisplayName()));
        statusColumn.setCellFactory(column -> new TableCell<>() {
            private final Label statusLabel = new Label();

            {
                statusLabel.getStyleClass().add("budget-status-pill");
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }

                Budget budget = getTableView().getItems().get(getIndex());
                statusLabel.setText(item);
                statusLabel.getStyleClass().setAll("budget-status-pill", statusStyleClass(budget.getStatus()));
                setGraphic(statusLabel);
                setText(null);
            }
        });
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
            budgetCardsGrid.add(createEmptyState(
                    "Belum ada anggaran",
                    "Buat anggaran pengeluaran untuk melihat batas, progres, dan status pemakaian."
            ), 0, 0, 2, 1);
            return;
        }

        for (int index = 0; index < budgets.size(); index++) {
            Budget budget = budgets.get(index);
            budgetCardsGrid.add(createBudgetCard(budget), index % 2, index / 2);
        }
    }

    private VBox createBudgetCard(Budget budget) {
        Label categoryLabel = new Label(budget.getCategoryName());
        categoryLabel.getStyleClass().add("budget-card-title");

        Label statusLabel = new Label(budget.getStatus().getDisplayName());
        statusLabel.getStyleClass().addAll("budget-status-pill", statusStyleClass(budget.getStatus()));

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        HBox topRow = new HBox(10, categoryLabel, topSpacer, statusLabel);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label limitCaptionLabel = new Label("Limit Anggaran");
        limitCaptionLabel.getStyleClass().add("budget-card-caption");

        Label limitLabel = new Label(formatCurrency(budget.getLimitAmount()));
        limitLabel.getStyleClass().add("budget-card-amount");

        ProgressBar progressBar = new ProgressBar(Math.min(1, budget.getUsagePercentage() / 100));
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.getStyleClass().addAll("budget-progress", progressStyleClass(budget.getStatus()));

        Label percentageLabel = new Label(String.format("%.0f%% terpakai", budget.getUsagePercentage()));
        percentageLabel.getStyleClass().add("budget-card-caption");

        Label usedLabel = new Label("Terpakai");
        usedLabel.getStyleClass().add("budget-card-caption");

        Label usedValueLabel = new Label(formatCurrency(budget.getUsedAmount()));
        usedValueLabel.getStyleClass().add("budget-card-detail");

        Label remainingLabel = new Label("Sisa");
        remainingLabel.getStyleClass().add("budget-card-caption");

        Label remainingValueLabel = new Label(formatCurrency(Math.max(0, budget.getLimitAmount() - budget.getUsedAmount())));
        remainingValueLabel.getStyleClass().add("budget-card-detail");

        VBox usedBox = new VBox(3, usedLabel, usedValueLabel);
        VBox remainingBox = new VBox(3, remainingLabel, remainingValueLabel);
        Region detailSpacer = new Region();
        HBox.setHgrow(detailSpacer, Priority.ALWAYS);

        HBox detailRow = new HBox(12, usedBox, detailSpacer, remainingBox);
        detailRow.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(12, topRow, limitCaptionLabel, limitLabel, progressBar, percentageLabel, detailRow);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().addAll("budget-card", "budget-summary-card", cardStatusClass(budget.getStatus()));
        return card;
    }

    private String statusStyleClass(BudgetStatus status) {
        return switch (status) {
            case AMAN -> "budget-status-safe";
            case MENDEKATI_LIMIT -> "budget-status-warning";
            case MELEBIHI_LIMIT -> "budget-status-danger";
        };
    }

    private String progressStyleClass(BudgetStatus status) {
        return switch (status) {
            case AMAN -> "budget-progress-safe";
            case MENDEKATI_LIMIT -> "budget-progress-warning";
            case MELEBIHI_LIMIT -> "budget-progress-danger";
        };
    }

    private String cardStatusClass(BudgetStatus status) {
        return switch (status) {
            case AMAN -> "budget-card-safe";
            case MENDEKATI_LIMIT -> "budget-card-warning";
            case MELEBIHI_LIMIT -> "budget-card-danger";
        };
    }

    private VBox createEmptyState(String title, String copy) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("empty-state-title");

        Label copyLabel = new Label(copy);
        copyLabel.getStyleClass().add("empty-state-copy");
        copyLabel.setWrapText(true);

        VBox emptyState = new VBox(6, titleLabel, copyLabel);
        emptyState.getStyleClass().add("empty-state-card");
        return emptyState;
    }
}
