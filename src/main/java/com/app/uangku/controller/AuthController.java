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

            if (usernameOrEmail.isBlank() || password.isBlank()) {
                setMessage(authMessageLabel, "Username/email dan password wajib diisi.");
                return;
            }

            User user = userDAO.login(usernameOrEmail, password)
                    .orElse(null);
            if (user == null) {
                setMessage(authMessageLabel, "Login gagal. Periksa username/email dan password.");
                return;
            }

            SessionManager.setCurrentUser(user);
            categoryDAO.createDefaultCategoriesForUser(user.getIdUser());
            SceneManager.switchTo(event, "dashboard.fxml");
        } catch (SQLException | IOException exception) {
            setMessage(authMessageLabel, "Terjadi kesalahan: " + exception.getMessage());
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            String username = registerUsernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = registerPasswordField.getText();

            if (username.isBlank() || email.isBlank() || password.isBlank()) {
                setMessage(registerMessageLabel, "Semua field wajib diisi.");
                return;
            }
            if (!email.contains("@")) {
                setMessage(registerMessageLabel, "Format email belum valid.");
                return;
            }
            if (password.length() < 6) {
                setMessage(registerMessageLabel, "Password minimal 6 karakter.");
                return;
            }
            if (userDAO.isUsernameTaken(username)) {
                setMessage(registerMessageLabel, "Username sudah digunakan.");
                return;
            }
            if (userDAO.isEmailTaken(email)) {
                setMessage(registerMessageLabel, "Email sudah digunakan.");
                return;
            }

            User user = userDAO.register(username, email, password);
            categoryDAO.createDefaultCategoriesForUser(user.getIdUser());
            SessionManager.setCurrentUser(user);
            SceneManager.switchTo(event, "dashboard.fxml");
        } catch (SQLException | IOException exception) {
            setMessage(registerMessageLabel, "Terjadi kesalahan: " + exception.getMessage());
        }
    }
}
