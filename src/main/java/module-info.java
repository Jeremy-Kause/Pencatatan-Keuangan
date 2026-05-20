module com.app.uangku {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;


    opens com.app.uangku to javafx.fxml;
    exports com.app.uangku;
    exports com.app.uangku.dao;
    exports com.app.uangku.model;
    exports com.app.uangku.util;
}
