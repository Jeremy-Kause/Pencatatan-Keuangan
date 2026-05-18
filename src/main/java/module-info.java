module com.app.pencatatan_keuangan {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.app.pencatatan_keuangan to javafx.fxml;
    exports com.app.pencatatan_keuangan;
}