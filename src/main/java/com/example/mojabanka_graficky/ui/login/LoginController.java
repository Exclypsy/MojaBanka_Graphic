package com.example.mojabanka_graficky.ui.login;

import com.example.mojabanka_graficky.HelloApplication;
import com.example.mojabanka_graficky.security.Session;
import com.example.mojabanka_graficky.security.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.LoadException;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * JavaFX kontrolér pre prihlasovaciu obrazovku (login-view.fxml).
 * Spracúva prihlasovací formulár – overuje údaje cez {@link AuthService}
 * a po úspešnom prihlásení presmeruje na správny dashboard.
 */
public class LoginController {

    /** Pole pre zadanie používateľského mena. */
    @FXML private TextField usernameField;

    /** Pole pre zadanie hesla (vstup je skrytý). */
    @FXML private PasswordField passwordField;

    /** Label na zobrazenie chybovej správy pri neúspešnom prihlásení. */
    @FXML private Label errorLabel;

    /** Autentifikačná služba – overuje prihlasovacie údaje voči databáze. */
    private final AuthService auth = new AuthService();

    /**
     * Obsluha kliknutia na tlačidlo "Prihlásiť".
     * Overí prihlasovacie údaje. Pri úspechu presmeruje na:
     * <ul>
     *   <li>admin-dashboard.fxml – ak je prihlásený ADMIN</li>
     *   <li>user-dashboard.fxml  – ak je prihlásený USER</li>
     * </ul>
     * Pri chybe zobrazí popis problému v {@code errorLabel}.
     */
    @FXML
    private void onLogin() {
        // Overenie prihlasovacích údajov cez AuthService
        boolean ok = auth.login(usernameField.getText().trim(), passwordField.getText());
        if (!ok) {
            errorLabel.setText("Nesprávne meno alebo heslo");
            return;
        }
        try {
            if (Session.isAdmin()) {
                // Načíta admin-dashboard.fxml z resources/com/example/mojabanka_graficky/
                HelloApplication.setRoot("admin-dashboard");
            } else {
                // Načíta user-dashboard.fxml
                HelloApplication.setRoot("user-dashboard");
            }
        } catch (Exception e) {
            e.printStackTrace();

            // Zostavenie podrobnej chybovej správy pre debugovanie
            String msg = e.getClass().getSimpleName() + ": " +
                    (e.getMessage() == null ? "" : e.getMessage());

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
