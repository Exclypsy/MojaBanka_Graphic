module com.example.mojabanka_graficky {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.example.mojabanka_graficky to javafx.fxml;
    exports com.example.mojabanka_graficky;
}