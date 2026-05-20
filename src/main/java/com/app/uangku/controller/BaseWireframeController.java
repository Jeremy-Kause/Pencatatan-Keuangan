package com.app.uangku.controller;

import com.app.uangku.util.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.io.IOException;

public abstract class BaseWireframeController {
    @FXML
    protected void showLogin(ActionEvent event) throws IOException {
        SceneManager.switchTo(event, "login.fxml");
    }

    @FXML
    protected void showRegister(ActionEvent event) throws IOException {
        SceneManager.switchTo(event, "register.fxml");
    }

    @FXML
    protected void showDashboard(ActionEvent event) throws IOException {
        SceneManager.switchTo(event, "dashboard.fxml");
    }

    @FXML
    protected void showTransactions(ActionEvent event) throws IOException {
        SceneManager.switchTo(event, "transactions.fxml");
    }

    @FXML
    protected void showCategories(ActionEvent event) throws IOException {
        SceneManager.switchTo(event, "categories.fxml");
    }

    @FXML
    protected void showBudgets(ActionEvent event) throws IOException {
        SceneManager.switchTo(event, "budgets.fxml");
    }

    @FXML
    protected void showReports(ActionEvent event) throws IOException {
        SceneManager.switchTo(event, "reports.fxml");
    }
}
