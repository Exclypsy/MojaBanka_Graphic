module com.example.mojabanka_graficky {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    requires org.kordamp.bootstrapfx.core;
    requires mysql.connector.j;

    opens com.example.mojabanka_graficky.ui.login to javafx.fxml;
    opens com.example.mojabanka_graficky.ui.user to javafx.fxml;
    opens com.example.mojabanka_graficky.ui.admin to javafx.fxml;

    opens com.example.mojabanka_graficky to javafx.fxml;
    exports com.example.mojabanka_graficky;
    opens com.example.mojabanka_graficky.model to javafx.fxml;
    exports com.example.mojabanka_graficky.model;
    exports com.example.mojabanka_graficky.ui.login;
}

