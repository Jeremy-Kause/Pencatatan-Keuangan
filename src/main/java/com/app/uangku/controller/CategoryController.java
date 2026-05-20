package com.app.uangku.controller;

import com.app.uangku.dao.CategoryDAO;
import com.app.uangku.model.Category;
import com.app.uangku.model.TransactionType;
import com.app.uangku.model.User;
import com.app.uangku.util.SessionManager;
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
    private void initialize() {
        categoryTypeComboBox.setItems(FXCollections.observableArrayList(TransactionType.values()));
        loadCategories();
    }

    @FXML
    private void handleSaveCategory() {
        if (!hasActiveSession()) {
            setMessage(categoryMessageLabel, "Silakan login terlebih dahulu.");
            return;
        }

        try {
            String name = categoryNameField.getText().trim();
            TransactionType type = categoryTypeComboBox.getValue();

            if (name.isBlank()) {
                setMessage(categoryMessageLabel, "Nama kategori wajib diisi.");
                return;
            }
            if (type == null) {
                setMessage(categoryMessageLabel, "Pilih tipe kategori.");
                return;
            }

            User user = SessionManager.getCurrentUser().orElseThrow();
            categoryDAO.create(new Category(user.getIdUser(), name, type));
            categoryNameField.clear();
            categoryTypeComboBox.setValue(null);
            loadCategories();
            setMessage(categoryMessageLabel, "Kategori berhasil disimpan.");
        } catch (SQLException exception) {
            setMessage(categoryMessageLabel, "Gagal menyimpan kategori: " + exception.getMessage());
        }
    }

    @FXML
    private void focusCategoryForm() {
        categoryNameField.requestFocus();
    }

    private void loadCategories() {
        incomeCategoryList.getChildren().clear();
        expenseCategoryList.getChildren().clear();

        if (!hasActiveSession()) {
            incomeCategoryList.getChildren().add(new Label("Silakan login terlebih dahulu"));
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
            setMessage(categoryMessageLabel, "Gagal memuat kategori: " + exception.getMessage());
        }
    }

    private void renderCategoryList(VBox container, List<Category> categories) {
        if (categories.isEmpty()) {
            Label emptyLabel = new Label("Belum ada kategori");
            emptyLabel.getStyleClass().add("muted-text");
            container.getChildren().add(emptyLabel);
            return;
        }

        for (Category category : categories) {
            container.getChildren().add(createCategoryRow(category));
        }
    }

    private HBox createCategoryRow(Category category) {
        Label nameLabel = new Label(category.getName());
        nameLabel.getStyleClass().add("placeholder-title");

        Button deleteButton = new Button("Hapus");
        deleteButton.getStyleClass().add("secondary-button");
        deleteButton.setOnAction(event -> deleteCategory(category));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(10, nameLabel, spacer, deleteButton);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("category-card");
        return row;
    }

    private void deleteCategory(Category category) {
        try {
            if (categoryDAO.isUsedByTransaction(category.getIdCategory(), category.getIdUser())) {
                setMessage(categoryMessageLabel, "Kategori masih dipakai transaksi, tidak bisa dihapus.");
                return;
            }

            categoryDAO.deleteById(category.getIdCategory(), category.getIdUser());
            loadCategories();
            setMessage(categoryMessageLabel, "Kategori berhasil dihapus.");
        } catch (SQLException exception) {
            setMessage(categoryMessageLabel, "Gagal menghapus kategori: " + exception.getMessage());
        }
    }
}
