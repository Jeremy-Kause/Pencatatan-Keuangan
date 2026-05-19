module com.app.uangku {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.app.uangku to javafx.fxml;
    exports com.app.uangku;
}