package com.app.uangku.controller;

import com.app.uangku.dao.SavingsGoalDAO;
import com.app.uangku.model.SavingsGoal;

import com.app.uangku.model.SavingsGoalStatus;
import com.app.uangku.model.User;
import com.app.uangku.util.SessionManager;
import com.app.uangku.validation.SavingsGoalInputValidator;
import com.app.uangku.validation.ValidationResult;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class SavingsGoalController extends BaseWireframeController {
    private final SavingsGoalDAO savingsGoalDAO = new SavingsGoalDAO();
    private final SavingsGoalInputValidator savingsGoalInputValidator = new SavingsGoalInputValidator();
    private SavingsGoal selectedGoal;

    @FXML
    private TextField goalNameField;

    @FXML
    private TextField goalTargetField;

    @FXML
    private DatePicker goalTargetDatePicker;

    @FXML
    private TextField goalCurrentAmountField;

    @FXML
    private TextArea goalDescriptionArea;

    @FXML
    private GridPane goalCardsGrid;

    @FXML
    private TableView<SavingsGoal> goalTable;

    @FXML
    private Label goalMessageLabel;

    @FXML
    private Button saveGoalButton;

    @FXML
    private Button cancelGoalButton;

    @FXML
    private void initialize() {
        setupTable();
        updateFormState();
        refreshGoals();
    }

    @FXML
    private void handleSaveGoal() {
        if (!hasActiveSession()) {
            setErrorMessage(goalMessageLabel, "Silakan login terlebih dahulu.");
            return;
        }

        try {
            String name = goalNameField.getText() == null ? "" : goalNameField.getText().trim();
            double targetAmount = parseAmount(goalTargetField.getText());
            double currentAmount = 0;
            if (goalCurrentAmountField.getText() != null && !goalCurrentAmountField.getText().isBlank()) {
                currentAmount = parseAmount(goalCurrentAmountField.getText());
            }
            LocalDate targetDate = goalTargetDatePicker.getValue();
            String description = goalDescriptionArea.getText() == null ? "" : goalDescriptionArea.getText().trim();
            clearMessage(goalMessageLabel);

            ValidationResult validationResult = savingsGoalInputValidator.validate(name, targetAmount, currentAmount, targetDate);
            if (!validationResult.isValid()) {
                setErrorMessage(goalMessageLabel, validationResult.getMessage());
                return;
            }

            User user = SessionManager.getCurrentUser().orElseThrow();
            SavingsGoal goal = new SavingsGoal(
                    user.getIdUser(),
                    name,
                    targetAmount,
                    currentAmount,
                    targetDate,
                    description.isBlank() ? null : description
            );

            if (selectedGoal == null) {
                savingsGoalDAO.create(goal);
                setSuccessMessage(goalMessageLabel, "Target tabungan berhasil disimpan.");
            } else {
                goal.setIdGoal(selectedGoal.getIdGoal());
                if (!savingsGoalDAO.update(goal)) {
                    setErrorMessage(goalMessageLabel, "Target tabungan tidak ditemukan.");
                    return;
                }
                setSuccessMessage(goalMessageLabel, "Target tabungan berhasil diperbarui.");
            }

            clearForm();
            refreshGoals();
        } catch (NumberFormatException exception) {
            setErrorMessage(goalMessageLabel, "Target nominal tidak valid.");
        } catch (IllegalArgumentException | SQLException exception) {
            setErrorMessage(goalMessageLabel, exception.getMessage());
        }
    }

    @FXML
    private void handleCancelGoalEdit() {
        clearForm();
        clearMessage(goalMessageLabel);
    }

    @FXML
    private void handleRefreshGoals() {
        refreshGoals();
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {
        if (goalTable == null || goalTable.getColumns().size() < 7) {
            return;
        }

        TableColumn<SavingsGoal, String> nameColumn = (TableColumn<SavingsGoal, String>) goalTable.getColumns().get(0);
        TableColumn<SavingsGoal, String> targetColumn = (TableColumn<SavingsGoal, String>) goalTable.getColumns().get(1);
        TableColumn<SavingsGoal, String> progressColumn = (TableColumn<SavingsGoal, String>) goalTable.getColumns().get(2);
        TableColumn<SavingsGoal, String> currentColumn = (TableColumn<SavingsGoal, String>) goalTable.getColumns().get(3);
        TableColumn<SavingsGoal, String> dateColumn = (TableColumn<SavingsGoal, String>) goalTable.getColumns().get(4);
        TableColumn<SavingsGoal, String> statusColumn = (TableColumn<SavingsGoal, String>) goalTable.getColumns().get(5);
        TableColumn<SavingsGoal, SavingsGoal> actionColumn = (TableColumn<SavingsGoal, SavingsGoal>) goalTable.getColumns().get(6);

        goalTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        targetColumn.setCellValueFactory(data -> new SimpleStringProperty(formatCurrency(data.getValue().getTargetAmount())));
        progressColumn.setCellValueFactory(data -> new SimpleStringProperty(String.format("%.0f%%", data.getValue().getProgressPercentage())));
        currentColumn.setCellValueFactory(data -> new SimpleStringProperty(formatCurrency(data.getValue().getCurrentAmount())));
        dateColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getTargetDate() == null ? "-" : data.getValue().getTargetDate().toString()
        ));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().getDisplayName()));
        actionColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));

        progressColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                SavingsGoal goal = getTableView().getItems().get(getIndex());
                ProgressBar bar = new ProgressBar(Math.min(1, goal.getProgressPercentage() / 100));
                bar.setPrefWidth(110);
                bar.getStyleClass().addAll("goal-progress", progressStyleClass(goal.getStatus()));

                Label percent = new Label(item);
                percent.getStyleClass().add("goal-progress-label");

                HBox wrapper = new HBox(10, bar, percent);
                wrapper.setAlignment(Pos.CENTER_LEFT);
                setGraphic(wrapper);
                setText(null);
            }
        });

        statusColumn.setCellFactory(column -> new TableCell<>() {
            private final Label statusLabel = new Label();

            {
                statusLabel.getStyleClass().add("goal-status-pill");
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }

                SavingsGoal goal = getTableView().getItems().get(getIndex());
                statusLabel.setText(item);
                statusLabel.getStyleClass().setAll("goal-status-pill", statusStyleClass(goal.getStatus()));
                setGraphic(statusLabel);
                setText(null);
            }
        });

        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Hapus");
            private final HBox actions = new HBox(8, editButton, deleteButton);

            {
                actions.setAlignment(Pos.CENTER_LEFT);
                actions.getStyleClass().add("table-actions");
                editButton.getStyleClass().addAll("table-action-button", "table-edit-button");
                deleteButton.getStyleClass().addAll("table-action-button", "table-delete-button");
                editButton.setOnAction(event -> startEditGoal(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(event -> deleteGoal(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(SavingsGoal item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actions);
                setText(null);
            }
        });
    }

    private void refreshGoals() {
        if (!hasActiveSession() || goalTable == null) {
            return;
        }

        try {
            User user = SessionManager.getCurrentUser().orElseThrow();
            List<SavingsGoal> goals = savingsGoalDAO.findByUserId(user.getIdUser());
            goalTable.setItems(FXCollections.observableArrayList(goals));
            renderGoalCards(goals);
            if (selectedGoal == null) {
                clearMessage(goalMessageLabel);
            }
        } catch (SQLException exception) {
            setErrorMessage(goalMessageLabel, "Gagal memuat target tabungan: " + exception.getMessage());
        }
    }

    private void renderGoalCards(List<SavingsGoal> goals) {
        goalCardsGrid.getChildren().clear();

        if (goals.isEmpty()) {
            goalCardsGrid.add(createEmptyState(
                    "Belum ada target",
                    "Tambahkan target tabungan untuk melihat progres, sisa nominal, dan status pencapaian."
            ), 0, 0, 2, 1);
            return;
        }

        for (int index = 0; index < goals.size(); index++) {
            goalCardsGrid.add(createGoalCard(goals.get(index)), index % 2, index / 2);
        }
    }

    private VBox createGoalCard(SavingsGoal goal) {
        Label titleLabel = new Label(goal.getName());
        titleLabel.getStyleClass().add("goal-card-title");

        Label statusLabel = new Label(goal.getStatus().getDisplayName());
        statusLabel.getStyleClass().addAll("goal-status-pill", statusStyleClass(goal.getStatus()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox headerRow = new HBox(10, titleLabel, spacer, statusLabel);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label targetCaption = new Label("Target");
        targetCaption.getStyleClass().add("goal-card-caption");

        Label targetValue = new Label(formatCurrency(goal.getTargetAmount()));
        targetValue.getStyleClass().add("goal-card-amount");

        Label progressCaption = new Label("Progres saat ini");
        progressCaption.getStyleClass().add("goal-card-caption");

        Label currentValue = new Label(formatCurrency(goal.getCurrentAmount()));
        currentValue.getStyleClass().add("goal-card-detail");

        ProgressBar progressBar = new ProgressBar(Math.min(1, goal.getProgressPercentage() / 100));
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.getStyleClass().addAll("goal-progress", progressStyleClass(goal.getStatus()));

        Label percentLabel = new Label(String.format("%.0f%% tercapai", goal.getProgressPercentage()));
        percentLabel.getStyleClass().add("goal-card-caption");

        Label remainingCaption = new Label("Sisa");
        remainingCaption.getStyleClass().add("goal-card-caption");

        Label remainingValue = new Label(formatCurrency(goal.getRemainingAmount()));
        remainingValue.getStyleClass().add("goal-card-detail");

        VBox topGroup = new VBox(4, targetCaption, targetValue);
        VBox progressGroup = new VBox(4, progressCaption, currentValue);
        VBox remainingGroup = new VBox(4, remainingCaption, remainingValue);

        HBox detailRow = new HBox(12, progressGroup, new Region(), remainingGroup);
        HBox.setHgrow(detailRow.getChildren().get(1), Priority.ALWAYS);
        detailRow.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(12, headerRow, topGroup, progressBar, percentLabel, detailRow);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().addAll("goal-card", "panel", goalStatusCardClass(goal.getStatus()));
        return card;
    }

    private void startEditGoal(SavingsGoal goal) {
        selectedGoal = goal;
        goalNameField.setText(goal.getName());
        goalTargetField.setText(String.valueOf(goal.getTargetAmount()));
        goalTargetDatePicker.setValue(goal.getTargetDate());
        goalCurrentAmountField.setText(String.valueOf(goal.getCurrentAmount()));
        goalDescriptionArea.setText(goal.getDescription() == null ? "" : goal.getDescription());
        updateFormState();
        setSuccessMessage(goalMessageLabel, "Sedang mengubah target yang dipilih.");
    }

    private void deleteGoal(SavingsGoal goal) {
        if (!confirmAction(
                "Hapus target",
                "Target tabungan \"" + goal.getName() + "\" akan dihapus permanen. Lanjutkan?",
                "Hapus"
        )) {
            return;
        }

        try {
            if (!savingsGoalDAO.deleteById(goal.getIdGoal(), goal.getIdUser())) {
                setErrorMessage(goalMessageLabel, "Target tabungan tidak ditemukan.");
                return;
            }

            if (selectedGoal != null && selectedGoal.getIdGoal() == goal.getIdGoal()) {
                clearForm();
            }

            refreshGoals();
            setSuccessMessage(goalMessageLabel, "Target tabungan berhasil dihapus.");
        } catch (SQLException exception) {
            setErrorMessage(goalMessageLabel, "Gagal menghapus target tabungan: " + exception.getMessage());
        }
    }

    private void clearForm() {
        selectedGoal = null;
        goalNameField.clear();
        goalTargetField.clear();
        goalTargetDatePicker.setValue(null);
        goalCurrentAmountField.clear();
        goalDescriptionArea.clear();
        updateFormState();
    }

    private void updateFormState() {
        boolean editing = selectedGoal != null;
        if (saveGoalButton != null) {
            saveGoalButton.setText(editing ? "Perbarui Target" : "Simpan Target");
        }
        if (cancelGoalButton != null) {
            cancelGoalButton.setVisible(editing);
            cancelGoalButton.setManaged(editing);
        }
    }

    private String statusStyleClass(SavingsGoalStatus status) {
        if (status == SavingsGoalStatus.ON_TRACK) {
            return "goal-status-safe";
        } else if (status == SavingsGoalStatus.NEAR_TARGET) {
            return "goal-status-warning";
        } else if (status == SavingsGoalStatus.ACHIEVED) {
            return "goal-status-success";
        }
        return "";
    }

    private String progressStyleClass(SavingsGoalStatus status) {
        if (status == SavingsGoalStatus.ON_TRACK) {
            return "goal-progress-safe";
        } else if (status == SavingsGoalStatus.NEAR_TARGET) {
            return "goal-progress-warning";
        } else if (status == SavingsGoalStatus.ACHIEVED) {
            return "goal-progress-success";
        }
        return "";
    }

    private String goalStatusCardClass(SavingsGoalStatus status) {
        if (status == SavingsGoalStatus.ON_TRACK) {
            return "goal-card-safe";
        } else if (status == SavingsGoalStatus.NEAR_TARGET) {
            return "goal-card-warning";
        } else if (status == SavingsGoalStatus.ACHIEVED) {
            return "goal-card-success";
        }
        return "";
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
