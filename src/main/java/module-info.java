module com.example.mojabanka_graficky {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.example.mojabanka_graficky to javafx.fxml;
    exports com.example.mojabanka_graficky;
    opens com.example.mojabanka_graficky.model to javafx.fxml;
    exports com.example.mojabanka_graficky.model;
    exports com.example.mojabanka_graficky.ui.login;
    opens com.example.mojabanka_graficky.ui.login to javafx.fxml;
}