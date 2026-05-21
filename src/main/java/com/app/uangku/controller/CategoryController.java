package com.app.uangku.controller;

import com.app.uangku.dao.CategoryDAO;
import com.app.uangku.model.Category;
import com.app.uangku.model.TransactionType;
import com.app.uangku.model.User;
import com.app.uangku.util.SessionManager;
import com.app.uangku.validation.CategoryInputValidator;
import com.app.uangku.validation.ValidationResult;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;

public class CategoryController extends BaseWireframeController {
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final CategoryInputValidator categoryInputValidator = new CategoryInputValidator();
    private Category selectedCategory;

    @FXML
    private TextField categoryNameField;

    @FXML
    private ComboBox<TransactionType> categoryTypeComboBox;

    @FXML
    private VBox incomeCategoryList;

    @FXML
    private VBox expenseCategoryList;

    @FXML
    private Label categoryMessageLabel;

    @FXML
    private Button saveCategoryButton;

    @FXML
    private Button cancelEditCategoryButton;

    @FXML
    private void initialize() {
        categoryTypeComboBox.setItems(FXCollections.observableArrayList(TransactionType.values()));
        updateFormState();
        loadCategories();
    }

    @FXML
    private void handleSaveCategory() {
        if (!hasActiveSession()) {
            setErrorMessage(categoryMessageLabel, "Silakan login terlebih dahulu.");
            return;
        }

        try {
            String name = categoryNameField.getText().trim();
            TransactionType type = categoryTypeComboBox.getValue();
            clearMessage(categoryMessageLabel);

            ValidationResult validationResult = categoryInputValidator.validate(name, type);
            if (!validationResult.isValid()) {
                setErrorMessage(categoryMessageLabel, validationResult.getMessage());
                return;
            }

            User user = SessionManager.getCurrentUser().orElseThrow();
            boolean duplicate = selectedCategory == null
                    ? categoryDAO.existsByUserIdAndTypeAndName(user.getIdUser(), type, name)
                    : categoryDAO.existsByUserIdAndTypeAndNameExceptId(
                    user.getIdUser(),
                    type,
                    name,
                    selectedCategory.getIdCategory()
            );
            if (duplicate) {
                setErrorMessage(categoryMessageLabel, "Kategori dengan nama yang sama sudah ada.");
                return;
            }

            if (selectedCategory == null) {
                categoryDAO.create(new Category(user.getIdUser(), name, type));
                setSuccessMessage(categoryMessageLabel, "Kategori berhasil disimpan.");
            } else {
                selectedCategory.setName(name);
                selectedCategory.setType(type);
                if (!categoryDAO.update(selectedCategory)) {
                    setErrorMessage(categoryMessageLabel, "Kategori tidak ditemukan.");
                    return;
                }
                setSuccessMessage(categoryMessageLabel, "Kategori berhasil diperbarui.");
            }

            clearForm();
            loadCategories();
        } catch (SQLException exception) {
            setErrorMessage(categoryMessageLabel, "Gagal menyimpan kategori: " + exception.getMessage());
        }
    }

    private void loadCategories() {
        incomeCategoryList.getChildren().clear();
        expenseCategoryList.getChildren().clear();

        if (!hasActiveSession()) {
            incomeCategoryList.getChildren().add(createEmptyState("Belum login", "Silakan login untuk melihat kategori pemasukan."));
            expenseCategoryList.getChildren().add(createEmptyState("Belum login", "Silakan login untuk melihat kategori pengeluaran."));
            return;
        }

        try {
            User user = SessionManager.getCurrentUser().orElseThrow();
            renderCategoryList(
                    incomeCategoryList,
                    categoryDAO.findByUserIdAndType(user.getIdUser(), TransactionType.PEMASUKAN)
            );
            renderCategoryList(
                    expenseCategoryList,
                    categoryDAO.findByUserIdAndType(user.getIdUser(), TransactionType.PENGELUARAN)
            );
        } catch (SQLException exception) {
            setErrorMessage(categoryMessageLabel, "Gagal memuat kategori: " + exception.getMessage());
        }
    }

    private void renderCategoryList(VBox container, List<Category> categories) {
        if (categories.isEmpty()) {
            container.getChildren().add(createEmptyState(
                    "Belum ada kategori",
                    "Kategori yang dibuat akan muncul di daftar ini."
            ));
            return;
        }

        for (Category category : categories) {
            container.getChildren().add(createCategoryRow(category));
        }
    }

    private HBox createCategoryRow(Category category) {
        Region marker = new Region();
        marker.getStyleClass().addAll(
                "category-marker",
                category.getType() == TransactionType.PEMASUKAN ? "category-income-marker" : "category-expense-marker"
        );

        Label nameLabel = new Label(category.getName());
        nameLabel.getStyleClass().add("category-row-title");

        Label typeLabel = new Label(category.getType().getDisplayName());
        typeLabel.getStyleClass().addAll(
                "type-pill",
                category.getType() == TransactionType.PEMASUKAN ? "type-income" : "type-expense"
        );

        VBox textGroup = new VBox(6, nameLabel, typeLabel);
        textGroup.getStyleClass().add("category-row-content");

        Button editButton = new Button("Edit");
        editButton.getStyleClass().addAll("table-action-button", "table-edit-button");
        editButton.setOnAction(event -> startEditCategory(category));

        Button deleteButton = new Button("Hapus");
        deleteButton.getStyleClass().addAll("table-action-button", "table-delete-button");
        deleteButton.setOnAction(event -> deleteCategory(category));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(12, marker, textGroup, spacer, editButton, deleteButton);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("category-row-card");
        return row;
    }

    private void deleteCategory(Category category) {
        try {
            if (categoryDAO.isUsedByTransaction(category.getIdCategory(), category.getIdUser())) {
                setErrorMessage(categoryMessageLabel, "Kategori masih dipakai transaksi, tidak bisa dihapus.");
                return;
            }
            if (!confirmAction(
                    "Hapus kategori",
                    "Kategori \"" + category.getName() + "\" akan dihapus permanen. Lanjutkan?",
                    "Hapus"
            )) {
                return;
            }

            categoryDAO.deleteById(category.getIdCategory(), category.getIdUser());
            if (selectedCategory != null && selectedCategory.getIdCategory() == category.getIdCategory()) {
                clearForm();
            }
            loadCategories();
            setSuccessMessage(categoryMessageLabel, "Kategori berhasil dihapus.");
        } catch (SQLException exception) {
            setErrorMessage(categoryMessageLabel, "Gagal menghapus kategori: " + exception.getMessage());
        }
    }

    @FXML
    private void handleCancelEditCategory() {
        clearForm();
        clearMessage(categoryMessageLabel);
    }

    private void startEditCategory(Category category) {
        selectedCategory = new Category(
                category.getIdCategory(),
                category.getIdUser(),
                category.getName(),
                category.getType()
        );
        categoryNameField.setText(category.getName());
        categoryTypeComboBox.setValue(category.getType());
        updateFormState();
        setSuccessMessage(categoryMessageLabel, "Sedang mengubah kategori yang dipilih.");
    }

    private void clearForm() {
        selectedCategory = null;
        categoryNameField.clear();
        categoryTypeComboBox.setValue(null);
        updateFormState();
    }

    private void updateFormState() {
        boolean editing = selectedCategory != null;
        if (saveCategoryButton != null) {
            saveCategoryButton.setText(editing ? "Perbarui Kategori" : "Simpan Kategori");
        }
        if (cancelEditCategoryButton != null) {
            cancelEditCategoryButton.setVisible(editing);
            cancelEditCategoryButton.setManaged(editing);
        }
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
