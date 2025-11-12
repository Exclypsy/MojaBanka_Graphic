package com.example.mojabanka_graficky.ui.user;

import com.example.mojabanka_graficky.dao.AccountDao;
import com.example.mojabanka_graficky.model.Ucet;
import com.example.mojabanka_graficky.model.UcetDoMinusu;
import com.example.mojabanka_graficky.security.Session;
import javafx.beans.property.*;
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
    @FXML private Label statusLabel;

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
        loadData();
    }

    private void loadData() {
        try {
            data.setAll(accountDao.findByUserId(Session.get().getId()));
            accountsTable.setItems(data);
        } catch (Exception e) {
            statusLabel.setText("Chyba načítania účtov");
        }
    }

    @FXML
    private void onDeposit() { mutateSelected(acc -> acc.vklad(parseAmount()), "Vklad prebehol"); }

    @FXML
    private void onWithdraw() { mutateSelected(acc -> acc.vyber(parseAmount()), "Výber prebehol"); }

    @FXML
    private void onApplyInterest() { mutateSelected(Ucet::zapocitajUrok, "Úrok započítaný"); }

    private double parseAmount() {
        try { return Double.parseDouble(amountField.getText().trim()); }
        catch (NumberFormatException e) { statusLabel.setText("Zadaj platnú sumu"); return 0; }
    }

    private void mutateSelected(java.util.function.Consumer<Ucet> op, String okMsg) {
        Ucet sel = accountsTable.getSelectionModel().getSelectedItem();
        if (sel == null) { statusLabel.setText("Vyber účet"); return; }
        op.accept(sel);
        try {
            accountDao.updateBalance(sel.getId(), sel.getZostatok());
            accountsTable.refresh();
            statusLabel.setText(okMsg);
        } catch (Exception e) { statusLabel.setText("Chyba ukladania"); }
    }
}
