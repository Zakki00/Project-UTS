module com.mycompany.projectuts {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mycompany.projectuts to javafx.fxml;
    exports com.mycompany.projectuts;
}
