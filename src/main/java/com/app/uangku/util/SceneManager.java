package com.app.uangku.util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public final class SceneManager {
    private static final String FXML_BASE = "/com/app/uangku/fxml/";

    private SceneManager() {
    }

    public static void switchTo(ActionEvent event, String fxmlName) throws IOException {
        URL resource = SceneManager.class.getResource(FXML_BASE + fxmlName);
        if (resource == null) {
            throw new IllegalStateException("FXML tidak ditemukan: " + fxmlName);
        }

        Parent root = FXMLLoader.load(resource);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root, 1100, 720);
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
    }
}
