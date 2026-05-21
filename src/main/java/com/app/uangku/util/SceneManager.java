package com.app.uangku.util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public final class SceneManager {
    private static final String FXML_BASE = "/com/app/uangku/fxml/";
    private static final String GLOBAL_STYLESHEET = "/com/app/uangku/css/global.css";
    private static final String FONT_BASE = "/com/app/uangku/fonts/";
    private static boolean fontsLoaded;

    private SceneManager() {
    }

    public static Scene createScene(Parent root) {
        Scene scene = new Scene(root, 1100, 720);
        applyGlobalTheme(scene);
        return scene;
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
            scene = createScene(root);
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
            applyGlobalTheme(scene);
        }
    }

    public static void applyGlobalTheme(Scene scene) {
        loadFonts();

        URL stylesheet = SceneManager.class.getResource(GLOBAL_STYLESHEET);
        if (stylesheet == null) {
            throw new IllegalStateException("CSS global tidak ditemukan: " + GLOBAL_STYLESHEET);
        }

        String stylesheetUrl = stylesheet.toExternalForm();
        if (!scene.getStylesheets().contains(stylesheetUrl)) {
            scene.getStylesheets().add(stylesheetUrl);
        }
    }

    private static void loadFonts() {
        if (fontsLoaded) {
            return;
        }

        loadFont("Poppins-Regular.ttf");
        loadFont("Poppins-Medium.ttf");
        loadFont("Poppins-SemiBold.ttf");
        loadFont("Poppins-Bold.ttf");
        loadFont("Poppins-ExtraBold.ttf");
        fontsLoaded = true;
    }

    private static void loadFont(String fileName) {
        URL resource = SceneManager.class.getResource(FONT_BASE + fileName);
        if (resource == null) {
            throw new IllegalStateException("Font tidak ditemukan: " + fileName);
        }
        Font.loadFont(resource.toExternalForm(), 13);
    }
}
