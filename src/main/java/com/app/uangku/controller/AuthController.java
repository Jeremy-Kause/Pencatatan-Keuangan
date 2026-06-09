package com.app.uangku.controller;

import com.app.uangku.dao.CategoryDAO;
import com.app.uangku.dao.UserDAO;
import com.app.uangku.model.User;
import com.app.uangku.util.SceneManager;
import com.app.uangku.util.SessionManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.SQLException;

public class AuthController extends BaseWireframeController {
    private final UserDAO userDAO = new UserDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField registerUsernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField registerPasswordField;

    @FXML
    private Label authMessageLabel;

    @FXML
    private Label registerMessageLabel;

    @FXML
    private void handleLogin(ActionEvent event) {
        try {
            String usernameOrEmail = usernameField.getText().trim();
            String password = passwordField.getText();
            clearMessage(authMessageLabel);

            if (usernameOrEmail.isEmpty()) {
                setErrorMessage(authMessageLabel, "Username atau email wajib diisi.");
                return;
            }
            if (password == null || password.isEmpty()) {
                setErrorMessage(authMessageLabel, "Password wajib diisi.");
                return;
            }

            User user = userDAO.login(usernameOrEmail, password)
                    .orElse(null);
            if (user == null) {
                setErrorMessage(authMessageLabel, "Login gagal. Periksa username/email dan password.");
                return;
            }

            SessionManager.setCurrentUser(user);
            categoryDAO.createDefaultCategoriesForUser(user.getIdUser());
            SceneManager.switchTo(event, "dashboard.fxml");
        } catch (SQLException | IOException exception) {
            setErrorMessage(authMessageLabel, "Terjadi kesalahan: " + exception.getMessage());
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            String username = registerUsernameField.getText().trim();
            String email = emailField.getText().trim().toLowerCase();
            String password = registerPasswordField.getText();
            clearMessage(registerMessageLabel);

            if (username.isEmpty()) {
                setErrorMessage(registerMessageLabel, "Username wajib diisi.");
                return;
            }
            if (username.length() < 4 || username.length() > 20) {
                setErrorMessage(registerMessageLabel, "Username harus antara 4 hingga 20 karakter.");
                return;
            }
            if (!username.matches("^[a-zA-Z0-9_]+$")) {
                setErrorMessage(registerMessageLabel, "Username hanya boleh berisi huruf, angka, dan underscore.");
                return;
            }
            if (email.isEmpty()) {
                setErrorMessage(registerMessageLabel, "Email wajib diisi.");
                return;
            }
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                setErrorMessage(registerMessageLabel, "Format email tidak valid.");
                return;
            }
            if (password == null || password.isEmpty()) {
                setErrorMessage(registerMessageLabel, "Password wajib diisi.");
                return;
            }
            if (password.length() < 6) {
                setErrorMessage(registerMessageLabel, "Password minimal 6 karakter.");
                return;
            }
            if (userDAO.isUsernameTaken(username)) {
                setErrorMessage(registerMessageLabel, "Username sudah digunakan.");
                return;
            }
            if (userDAO.isEmailTaken(email)) {
                setErrorMessage(registerMessageLabel, "Email sudah digunakan.");
                return;
            }

            User user = userDAO.register(username, email, password);
            categoryDAO.createDefaultCategoriesForUser(user.getIdUser());
            SessionManager.setCurrentUser(user);
            SceneManager.switchTo(event, "dashboard.fxml");
        } catch (SQLException | IOException exception) {
            setErrorMessage(registerMessageLabel, "Terjadi kesalahan: " + exception.getMessage());
        }
    }
}
