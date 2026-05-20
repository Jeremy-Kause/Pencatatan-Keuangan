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

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class TransactionController extends BaseWireframeController {
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final ObservableList<Category> allCategories = FXCollections.observableArrayList();

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
    private TableView<Transaction> transactionsTable;

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

            transactionDAO.create(new Transaction(
                    user.getIdUser(),
                    category.getIdCategory(),
                    parseAmount(amountField.getText()),
                    date,
                    descriptionArea.getText(),
                    type
            ));

            clearForm();
            refreshTransactions();
            setMessage(transactionMessageLabel, "Transaksi berhasil disimpan.");
        } catch (NumberFormatException exception) {
            setMessage(transactionMessageLabel, "Nominal tidak valid.");
        } catch (IllegalArgumentException | SQLException exception) {
            setMessage(transactionMessageLabel, exception.getMessage());
        }
    }

    @FXML
    private void focusTransactionForm() {
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
    }

    private void setupForm() {
        transactionTypeComboBox.setItems(FXCollections.observableArrayList(TransactionType.values()));
        transactionTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> refreshFormCategories());
        transactionDatePicker.setValue(LocalDate.now());
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

        dateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate().toString()));
        categoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategoryName()));
        typeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType().getDisplayName()));
        amountColumn.setCellValueFactory(data -> new SimpleStringProperty(formatCurrency(data.getValue().getAmount())));
        descriptionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button deleteButton = new Button("Hapus");

            {
                deleteButton.getStyleClass().add("secondary-button");
                deleteButton.setOnAction(event -> {
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    deleteTransaction(transaction);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
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
        transactionCategoryComboBox.setValue(null);
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
            transactionsTable.setItems(FXCollections.observableArrayList(
                    transactionDAO.filter(
                            user.getIdUser(),
                            null,
                            null,
                            type,
                            idCategory,
                            searchTransactionField.getText()
                    )
            ));
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
            refreshTransactions();
            setMessage(transactionMessageLabel, "Transaksi berhasil dihapus.");
        } catch (SQLException exception) {
            setMessage(transactionMessageLabel, "Gagal menghapus transaksi: " + exception.getMessage());
        }
    }

    private void clearForm() {
        amountField.clear();
        descriptionArea.clear();
        transactionDatePicker.setValue(LocalDate.now());
    }
}
