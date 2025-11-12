package com.example.mojabanka_graficky.ui.login;

import com.example.mojabanka_graficky.HelloApplication;
import com.example.mojabanka_graficky.security.Session;
import com.example.mojabanka_graficky.security.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.fxml.LoadException;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AuthService auth = new AuthService();

    @FXML
    private void onLogin() {
        boolean ok = auth.login(usernameField.getText().trim(), passwordField.getText());
        if (!ok) {
            errorLabel.setText("Nesprávne meno alebo heslo");
            return;
        }
        try {
            if (Session.isAdmin()) {
                HelloApplication.setRoot("admin-dashboard");
            } else {
                HelloApplication.setRoot("user-dashboard");
            }
        } catch (Exception e) {
            // 1) vypíš celý stack trace do konzoly
            e.printStackTrace();

            // 2) zostav užitočnú správu pre UI
            String msg = e.getClass().getSimpleName() + ": " + (e.getMessage() == null ? "" : e.getMessage());

            // 3) špecifické tipy pre časté FXML problémy
            if (e instanceof LoadException) {
                msg += " | Skontroluj fx:controller, <?import ...?> a cestu k FXML.";
            } else if (e instanceof NullPointerException) {
                msg += " | Možný nesúlad fx:id vs. @FXML polí v kontroléri.";
            } else if (msg.contains("null URL") || msg.contains("not found")) {
                msg += " | FXML resource neexistuje na ceste /com/example/mojabanka_graficky/*.fxml.";
            }

            errorLabel.setText("Chyba pri načítaní scény: " + msg);
        }
    }
}
