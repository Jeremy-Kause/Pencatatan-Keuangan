package com.app.uangku;

import com.app.uangku.util.DatabaseHelper;
import com.app.uangku.util.SceneManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        DatabaseHelper.initializeDatabase();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("fxml/login.fxml"));
        Scene scene = SceneManager.createScene(fxmlLoader.load());
        stage.setTitle("UangKu");
        stage.setMinWidth(980);
        stage.setMinHeight(640);
        stage.setScene(scene);
        stage.show();
    }
}
