package com.example.mojabanka_graficky;

import com.mysql.cj.x.protobuf.MysqlxDatatypes;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    private static Scene scene;

    @Override
    public void start(Stage stage) throws Exception {
        scene = new Scene(
                FXMLLoader.load(HelloApplication.class.getResource("/com/example/mojabanka_graficky/login-view.fxml")),
                1280, 900
        );
        stage.setTitle("Moja Banka");
        stage.setScene(scene);
        stage.show();
    }

    public static void setRoot(String name) throws Exception {
        scene.setRoot(FXMLLoader.load(HelloApplication.class.getResource("/com/example/mojabanka_graficky/" + name + ".fxml")));
    }


    public static void main(String[] args) {
        launch(args);
    }
}
