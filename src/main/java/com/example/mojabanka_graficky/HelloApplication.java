package com.example.mojabanka_graficky;

import com.mysql.cj.x.protobuf.MysqlxDatatypes;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Hlavná trieda aplikácie MojaBanka.
 * Rozširuje JavaFX triedu Application, čím sa stáva vstupným bodom grafickej aplikácie.
 * Pri spustení načíta prihlasovací FXML súbor a zobrazí hlavné okno.
 */
public class HelloApplication extends Application {

    /** Aktuálne zobrazená scéna (obrazovka) aplikácie – zdieľaná medzi celou appkou. */
    private static Scene scene;

    /**
     * Metóda volaná automaticky JavaFX pri štarte aplikácie.
     * Načíta login-view.fxml, vytvorí scénu s rozmerom 1280×900 px,
     * nastaví titulok okna a zobrazí ho.
     *
     * @param stage hlavné okno (Stage) poskytnuté JavaFX rámcom
     * @throws Exception ak sa nepodarí načítať FXML súbor
     */
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

    /**
     * Prepne obsah aktuálnej scény na iný FXML súbor.
     * Používa sa na navigáciu medzi obrazovkami (login → admin/user dashboard a späť).
     *
     * @param name názov FXML súboru BEZ prípony .fxml
     *             (napr. "admin-dashboard", "user-dashboard", "login-view")
     * @throws Exception ak FXML súbor neexistuje alebo sa nedá načítať
     */
    public static void setRoot(String name) throws Exception {
        scene.setRoot(FXMLLoader.load(HelloApplication.class.getResource("/com/example/mojabanka_graficky/" + name + ".fxml")));
    }

    /**
     * Vstupný bod JVM – spúšťa JavaFX aplikáciu.
     *
     * @param args argumenty príkazového riadka (nie sú využívané)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
