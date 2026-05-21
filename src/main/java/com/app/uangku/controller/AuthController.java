package com.app.uangku.controller;

import com.app.uangku.dao.CategoryDAO;
import com.app.uangku.dao.UserDAO;
import com.app.uangku.model.User;
import com.app.uangku.util.SceneManager;
import com.app.uangku.util.SessionManager;
import com.app.uangku.validation.AuthInputValidator;
import com.app.uangku.validation.ValidationResult;
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
    private final AuthInputValidator authInputValidator = new AuthInputValidator();

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

            ValidationResult validationResult = authInputValidator.validateLogin(usernameOrEmail, password);
            if (!validationResult.isValid()) {
                setErrorMessage(authMessageLabel, validationResult.getMessage());
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

            ValidationResult validationResult = authInputValidator.validateRegistration(username, email, password);
            if (!validationResult.isValid()) {
                setErrorMessage(registerMessageLabel, validationResult.getMessage());
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
