module com.mycompany.projectuts {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires java.sql;
    requires java.desktop;

    opens com.mycompany.projectuts to javafx.fxml;

    exports com.mycompany.projectuts;
}
