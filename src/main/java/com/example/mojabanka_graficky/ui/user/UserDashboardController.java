package com.example.mojabanka_graficky.ui.user;

import com.example.mojabanka_graficky.HelloApplication;
import com.example.mojabanka_graficky.dao.AccountDao;
import com.example.mojabanka_graficky.model.Ucet;
import com.example.mojabanka_graficky.model.UcetDoMinusu;
import com.example.mojabanka_graficky.security.Session;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class UserDashboardController {

    @FXML private TableView<Ucet> accountsTable;
    @FXML private TableColumn<Ucet, String> colOwner;
    @FXML private TableColumn<Ucet, Number> colNumber;
    @FXML private TableColumn<Ucet, Number> colBalance;
    @FXML private TableColumn<Ucet, Number> colInterest;
    @FXML private TableColumn<Ucet, String> colType;

    @FXML private TextField amountField;
    @FXML private TextField transferTargetField;
    @FXML private TextField transferAmountField;

    @FXML private Label statusLabel;
    @FXML private Label userLabel;

    private final AccountDao accountDao = new AccountDao();
    private final ObservableList<Ucet> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colOwner.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMajitel()));
        colNumber.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().getNumber()));
        colBalance.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getZostatok()));
        colInterest.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getUrok()));
        colType.setCellValueFactory(c -> new SimpleStringProperty(
                (c.getValue() instanceof UcetDoMinusu) ? "OVERDRAFT" : "STANDARD"
        ));

        if (Session.get() != null) {
            userLabel.setText("Prihlásený: " + Session.get().getUsername());
        }

        loadData();
    }

    private void loadData() {
        try {
            data.setAll(accountDao.findByUserId(Session.get().getId()));
            accountsTable.setItems(data);
            statusLabel.setText("");
        } catch (Exception e) {
            statusLabel.setText("Chyba načítania účtov");
        }
    }

    @FXML
    private void onDeposit() {
        double amount = parseAmount(amountField);
        if (amount <= 0) return;
        mutateSelected(acc -> acc.vklad(amount), "Vklad prebehol");
    }

    @FXML
    private void onWithdraw() {
        double amount = parseAmount(amountField);
        if (amount <= 0) return;
        mutateSelected(acc -> acc.vyber(amount), "Výber prebehol");
    }

    @FXML
    private void onApplyInterest() {
        mutateSelected(Ucet::zapocitajUrok, "Úrok započítaný");
    }

    @FXML
    private void onTransfer() {
        Ucet from = accountsTable.getSelectionModel().getSelectedItem();
        if (from == null) {
            statusLabel.setText("Vyber zdrojový účet.");
            return;
        }

        long targetNumber;
        try {
            targetNumber = Long.parseLong(transferTargetField.getText().trim());
        } catch (NumberFormatException e) {
            statusLabel.setText("Zadaj platné číslo cieľového účtu.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(transferAmountField.getText().trim());
        } catch (NumberFormatException e) {
            statusLabel.setText("Zadaj platnú sumu.");
            return;
        }
        if (amount <= 0) {
            statusLabel.setText("Suma musí byť väčšia ako 0.");
            return;
        }

        try {
            Ucet to = accountDao.findByNumber(targetNumber);
            if (to == null) {
                statusLabel.setText("Cieľový účet neexistuje.");
                return;
            }

            if (to.getId() == from.getId()) {
                statusLabel.setText("Nemôžeš poslať peniaze na ten istý účet.");
                return;
            }

            if (!(from instanceof UcetDoMinusu) && from.getZostatok() < amount) {
                statusLabel.setText("Nedostatočný zostatok.");
                return;
            }

            from.vyber(amount);
            to.vklad(amount);

            accountDao.updateBalance(from.getId(), from.getZostatok());
            accountDao.updateBalance(to.getId(), to.getZostatok());

            accountsTable.refresh();
            statusLabel.setText("Prevod " + amount + " na účet " + targetNumber + " prebehol.");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Chyba prevodu: " + e.getMessage());
        }
    }

    private double parseAmount(TextField field) {
        try {
            return Double.parseDouble(field.getText().trim());
        } catch (NumberFormatException e) {
            statusLabel.setText("Zadaj platnú sumu.");
            return 0;
        }
    }

    private void mutateSelected(java.util.function.Consumer<Ucet> op, String okMsg) {
        Ucet sel = accountsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            statusLabel.setText("Vyber účet.");
            return;
        }
        op.accept(sel);
        try {
            accountDao.updateBalance(sel.getId(), sel.getZostatok());
            accountsTable.refresh();
            statusLabel.setText(okMsg);
        } catch (Exception e) {
            statusLabel.setText("Chyba ukladania.");
        }
    }

    @FXML
    private void onLogout() {
        try {
            Session.clear();
            HelloApplication.setRoot("login-view");
        } catch (Exception ignored) {}
    }
}
