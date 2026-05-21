module com.app.uangku {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.app.uangku to javafx.fxml;
    opens com.app.uangku.controller to javafx.fxml;
    exports com.app.uangku;
    exports com.app.uangku.controller;
    exports com.app.uangku.dao;
    exports com.app.uangku.model;
    exports com.app.uangku.util;
}
