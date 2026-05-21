package com.app.uangku.controller;

import com.app.uangku.dao.CategoryDAO;
import com.app.uangku.dao.TransactionDAO;
import com.app.uangku.model.Category;
import com.app.uangku.model.Transaction;
import com.app.uangku.model.TransactionType;
import com.app.uangku.model.User;
import com.app.uangku.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class TransactionController extends BaseWireframeController {
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final ObservableList<Category> allCategories = FXCollections.observableArrayList();
    private Transaction selectedTransaction;

    @FXML
    private TextField searchTransactionField;

    @FXML
    private ToggleButton allFilterButton;

    @FXML
    private ToggleButton incomeFilterButton;

    @FXML
    private ToggleButton expenseFilterButton;

    @FXML
    private ComboBox<Category> categoryFilterComboBox;

    @FXML
    private DatePicker startDateFilterPicker;

    @FXML
    private DatePicker endDateFilterPicker;

    @FXML
    private TableView<Transaction> transactionsTable;

    @FXML
    private VBox transactionTablePanel;

    @FXML
    private VBox transactionFormPanel;

    @FXML
    private ComboBox<TransactionType> transactionTypeComboBox;

    @FXML
    private ComboBox<Category> transactionCategoryComboBox;

    @FXML
    private DatePicker transactionDatePicker;

    @FXML
    private TextField amountField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private Label transactionMessageLabel;

    @FXML
    private Button saveTransactionButton;

    @FXML
    private Button cancelEditButton;

    @FXML
    private void initialize() {
        setupFilters();
        setupForm();
        setupTable();
        loadCategories();
        refreshTransactions();
    }

    @FXML
    private void handleSaveTransaction() {
        if (!hasActiveSession()) {
            setMessage(transactionMessageLabel, "Silakan login terlebih dahulu.");
            return;
        }

        try {
            User user = SessionManager.getCurrentUser().orElseThrow();
            TransactionType type = transactionTypeComboBox.getValue();
            Category category = transactionCategoryComboBox.getValue();
            LocalDate date = transactionDatePicker.getValue();
            setMessage(transactionMessageLabel, "");

            if (type == null) {
                setMessage(transactionMessageLabel, "Pilih tipe transaksi.");
                return;
            }
            if (category == null) {
                setMessage(transactionMessageLabel, "Pilih kategori transaksi.");
                return;
            }
            if (date == null) {
                setMessage(transactionMessageLabel, "Pilih tanggal transaksi.");
                return;
            }
            if (category.getType() != type) {
                setMessage(transactionMessageLabel, "Kategori harus sesuai dengan tipe transaksi yang dipilih.");
                return;
            }
            String description = descriptionArea.getText() == null ? "" : descriptionArea.getText().trim();
            if (description.length() > 255) {
                setMessage(transactionMessageLabel, "Deskripsi maksimal 255 karakter.");
                return;
            }

            Transaction transaction = new Transaction(
                    user.getIdUser(),
                    category.getIdCategory(),
                    parseAmount(amountField.getText()),
                    date,
                    description,
                    type
            );

            if (selectedTransaction == null) {
                transactionDAO.create(transaction);
                setMessage(transactionMessageLabel, "Transaksi berhasil disimpan.");
            } else {
                transaction.setIdTransaction(selectedTransaction.getIdTransaction());
                if (!transactionDAO.update(transaction)) {
                    setMessage(transactionMessageLabel, "Transaksi tidak ditemukan.");
                    return;
                }
                setMessage(transactionMessageLabel, "Transaksi berhasil diperbarui.");
            }

            clearForm();
            refreshTransactions();
        } catch (NumberFormatException exception) {
            setMessage(transactionMessageLabel, "Nominal tidak valid.");
        } catch (IllegalArgumentException | SQLException exception) {
            setMessage(transactionMessageLabel, exception.getMessage());
        }
    }

    @FXML
    private void focusTransactionForm() {
        clearForm(false);
        setMessage(transactionMessageLabel, "");
        transactionTypeComboBox.requestFocus();
    }

    private void setupFilters() {
        ToggleGroup filterGroup = new ToggleGroup();
        allFilterButton.setToggleGroup(filterGroup);
        incomeFilterButton.setToggleGroup(filterGroup);
        expenseFilterButton.setToggleGroup(filterGroup);
        allFilterButton.setSelected(true);

        filterGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                allFilterButton.setSelected(true);
            }
            refreshTransactions();
        });
        searchTransactionField.textProperty().addListener((observable, oldValue, newValue) -> refreshTransactions());
        categoryFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> refreshTransactions());
        startDateFilterPicker.valueProperty().addListener((observable, oldValue, newValue) -> refreshTransactions());
        endDateFilterPicker.valueProperty().addListener((observable, oldValue, newValue) -> refreshTransactions());
    }

    private void setupForm() {
        transactionTypeComboBox.setItems(FXCollections.observableArrayList(TransactionType.values()));
        transactionTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> refreshFormCategories());
        transactionDatePicker.setValue(LocalDate.now());
        setTransactionFormVisible(false);
        updateFormState();
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {
        if (transactionsTable == null || transactionsTable.getColumns().isEmpty()) {
            return;
        }

        TableColumn<Transaction, String> dateColumn = (TableColumn<Transaction, String>) transactionsTable.getColumns().get(0);
        TableColumn<Transaction, String> categoryColumn = (TableColumn<Transaction, String>) transactionsTable.getColumns().get(1);
        TableColumn<Transaction, String> typeColumn = (TableColumn<Transaction, String>) transactionsTable.getColumns().get(2);
        TableColumn<Transaction, String> amountColumn = (TableColumn<Transaction, String>) transactionsTable.getColumns().get(3);
        TableColumn<Transaction, String> descriptionColumn = (TableColumn<Transaction, String>) transactionsTable.getColumns().get(4);
        TableColumn<Transaction, Void> actionColumn = (TableColumn<Transaction, Void>) transactionsTable.getColumns().get(5);

        transactionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        dateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate().toString()));
        categoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategoryName()));
        typeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType().getDisplayName()));
        amountColumn.setCellValueFactory(data -> new SimpleStringProperty(formatCurrency(data.getValue().getAmount())));
        descriptionColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDescription() == null || data.getValue().getDescription().isBlank()
                        ? "-"
                        : data.getValue().getDescription()
        ));
        typeColumn.setCellFactory(column -> new TableCell<>() {
            private final Label typeLabel = new Label();

            {
                typeLabel.getStyleClass().add("type-pill");
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }

                Transaction transaction = getTableView().getItems().get(getIndex());
                typeLabel.setText(item);
                typeLabel.getStyleClass().setAll(
                        "type-pill",
                        transaction.getType() == TransactionType.PEMASUKAN ? "type-income" : "type-expense"
                );
                setGraphic(typeLabel);
                setText(null);
            }
        });
        amountColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("amount-income", "amount-expense");
                if (empty || item == null || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setText(null);
                    return;
                }

                Transaction transaction = getTableView().getItems().get(getIndex());
                setText(item);
                getStyleClass().add(transaction.getType() == TransactionType.PEMASUKAN
                        ? "amount-income"
                        : "amount-expense");
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
                editButton.setOnAction(event -> {
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    startEditTransaction(transaction);
                });
                deleteButton.setOnAction(event -> {
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    deleteTransaction(transaction);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actions);
            }
        });
    }

    private void loadCategories() {
        if (!hasActiveSession()) {
            return;
        }

        try {
            User user = SessionManager.getCurrentUser().orElseThrow();
            allCategories.setAll(categoryDAO.findByUserId(user.getIdUser()));

            Category allCategory = new Category(0, user.getIdUser(), "Semua Kategori", null);
            ObservableList<Category> filterCategories = FXCollections.observableArrayList();
            filterCategories.add(allCategory);
            filterCategories.addAll(allCategories);
            categoryFilterComboBox.setItems(filterCategories);
            categoryFilterComboBox.setValue(allCategory);
            refreshFormCategories();
        } catch (SQLException exception) {
            setMessage(transactionMessageLabel, "Gagal memuat kategori: " + exception.getMessage());
        }
    }

    private void refreshFormCategories() {
        TransactionType selectedType = transactionTypeComboBox.getValue();
        List<Category> categories = allCategories.stream()
                .filter(category -> selectedType == null || category.getType() == selectedType)
                .toList();
        transactionCategoryComboBox.setItems(FXCollections.observableArrayList(categories));
        if (selectedTransaction == null || selectedType != selectedTransaction.getType()) {
            transactionCategoryComboBox.setValue(null);
        }
        if (selectedType != null && categories.isEmpty()) {
            setMessage(transactionMessageLabel, "Belum ada kategori untuk tipe transaksi ini.");
        }
    }

    private void refreshTransactions() {
        if (!hasActiveSession() || transactionsTable == null) {
            return;
        }

        try {
            User user = SessionManager.getCurrentUser().orElseThrow();
            TransactionType type = selectedFilterType();
            Category selectedCategory = categoryFilterComboBox.getValue();
            Integer idCategory = selectedCategory == null || selectedCategory.getIdCategory() == 0
                    ? null
                    : selectedCategory.getIdCategory();
            LocalDate startDate = startDateFilterPicker.getValue();
            LocalDate endDate = endDateFilterPicker.getValue();
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                setMessage(transactionMessageLabel, "Tanggal mulai tidak boleh melebihi tanggal akhir.");
                transactionsTable.setItems(FXCollections.emptyObservableList());
                return;
            }
            transactionsTable.setItems(FXCollections.observableArrayList(
                    transactionDAO.filter(
                            user.getIdUser(),
                            startDate,
                            endDate,
                            type,
                            idCategory,
                            searchTransactionField.getText()
                    )
            ));
            if (selectedTransaction == null) {
                setMessage(transactionMessageLabel, "");
            }
        } catch (SQLException exception) {
            setMessage(transactionMessageLabel, "Gagal memuat transaksi: " + exception.getMessage());
        }
    }

    private TransactionType selectedFilterType() {
        if (incomeFilterButton.isSelected()) {
            return TransactionType.PEMASUKAN;
        }
        if (expenseFilterButton.isSelected()) {
            return TransactionType.PENGELUARAN;
        }
        return null;
    }

    private void deleteTransaction(Transaction transaction) {
        try {
            transactionDAO.deleteById(transaction.getIdTransaction(), transaction.getIdUser());
            if (selectedTransaction != null && selectedTransaction.getIdTransaction() == transaction.getIdTransaction()) {
                clearForm();
            }
            refreshTransactions();
            setMessage(transactionMessageLabel, "Transaksi berhasil dihapus.");
        } catch (SQLException exception) {
            setMessage(transactionMessageLabel, "Gagal menghapus transaksi: " + exception.getMessage());
        }
    }

    @FXML
    private void handleCancelEdit() {
        clearForm();
        setMessage(transactionMessageLabel, "");
    }

    private void startEditTransaction(Transaction transaction) {
        setTransactionFormVisible(true);
        selectedTransaction = transaction;
        transactionTypeComboBox.setValue(transaction.getType());
        refreshFormCategories();
        transactionCategoryComboBox.getItems().stream()
                .filter(category -> category.getIdCategory() == transaction.getIdCategory())
                .findFirst()
                .ifPresent(transactionCategoryComboBox::setValue);
        transactionDatePicker.setValue(transaction.getDate());
        amountField.setText(String.valueOf(transaction.getAmount()));
        descriptionArea.setText(transaction.getDescription() == null ? "" : transaction.getDescription());
        updateFormState();
        transactionTypeComboBox.requestFocus();
        setMessage(transactionMessageLabel, "Sedang mengubah transaksi yang dipilih.");
    }

    private void clearForm() {
        clearForm(true);
    }

    private void clearForm(boolean hideForm) {
        selectedTransaction = null;
        transactionTypeComboBox.setValue(null);
        transactionCategoryComboBox.setValue(null);
        amountField.clear();
        descriptionArea.clear();
        transactionDatePicker.setValue(LocalDate.now());
        setTransactionFormVisible(!hideForm);
        updateFormState();
    }

    private void setTransactionFormVisible(boolean visible) {
        if (transactionFormPanel != null) {
            transactionFormPanel.setVisible(visible);
            transactionFormPanel.setManaged(visible);
        }
        if (transactionTablePanel != null) {
            GridPane.setColumnSpan(transactionTablePanel, visible ? 1 : 2);
        }
    }

    private void updateFormState() {
        boolean editing = selectedTransaction != null;
        boolean formVisible = transactionFormPanel == null || transactionFormPanel.isVisible();
        if (saveTransactionButton != null) {
            saveTransactionButton.setText(editing ? "Perbarui" : "Simpan");
        }
        if (cancelEditButton != null) {
            cancelEditButton.setText(editing ? "Batal Edit" : "Batal");
            cancelEditButton.setVisible(formVisible);
            cancelEditButton.setManaged(formVisible);
        }
    }
}
