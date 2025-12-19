package com.example.mojabanka_graficky.ui.user;

import com.example.mojabanka_graficky.HelloApplication;
import com.example.mojabanka_graficky.dao.AccountDao;
import com.example.mojabanka_graficky.dao.TransactionDao;
import com.example.mojabanka_graficky.dao.TransactionDao.TransactionUserView;
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

import java.time.format.DateTimeFormatter;

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

    // tabuľka transakcií používateľa
    @FXML private TableView<TransactionUserView> transactionsTable;
    @FXML private TableColumn<TransactionUserView, String> colTrCreated;
    @FXML private TableColumn<TransactionUserView, String> colTrAccount;
    @FXML private TableColumn<TransactionUserView, String> colTrType;
    @FXML private TableColumn<TransactionUserView, Number> colTrAmount;
    @FXML private TableColumn<TransactionUserView, Number> colTrBalanceAfter;
    @FXML private TableColumn<TransactionUserView, String> colTrRelatedAccount;
    @FXML private TableColumn<TransactionUserView, String> colTrDescription;

    private final AccountDao accountDao = new AccountDao();
    private final TransactionDao transactionDao = new TransactionDao();

    private final ObservableList<Ucet> data = FXCollections.observableArrayList();
    private final ObservableList<TransactionUserView> transactionsData = FXCollections.observableArrayList();

    private final DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

        // stĺpce transakcií
        colTrCreated.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().createdAt().format(dtFmt)));
        colTrAccount.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().accountNumber()));
        colTrType.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().operationType()));
        colTrAmount.setCellValueFactory(c ->
                new SimpleDoubleProperty(c.getValue().amount()));
        colTrBalanceAfter.setCellValueFactory(c ->
                new SimpleDoubleProperty(c.getValue().balanceAfter()));
        colTrRelatedAccount.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().relatedAccountNumber()));
        colTrDescription.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().description()));

        loadData();
        loadUserTransactions();
    }

    private void loadData() {
        try {
            data.setAll(accountDao.findByUserId(Session.get().getId()));
            accountsTable.setItems(data);
            statusLabel.setText("");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Chyba načítania účtov: " +
                    e.getClass().getSimpleName() + " " + e.getMessage());
        }
    }

    private void loadUserTransactions() {
        try {
            transactionsData.setAll(transactionDao.findForUser(Session.get().getId()));
            transactionsTable.setItems(transactionsData);
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Chyba načítania transakcií: " +
                    e.getClass().getSimpleName() + " " + e.getMessage());
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

            Integer userId = (Session.get() != null) ? Session.get().getId() : null;

            transactionDao.logTransaction(
                    userId,
                    from.getId(),
                    "TRANSFER_DEBIT",
                    amount,
                    from.getZostatok(),
                    to.getId(),
                    "Prevod na účet " + targetNumber
            );

            transactionDao.logTransaction(
                    userId,
                    to.getId(),
                    "TRANSFER_CREDIT",
                    amount,
                    to.getZostatok(),
                    from.getId(),
                    "Prijatý prevod z účtu " + from.getNumber()
            );

            accountsTable.refresh();
            loadUserTransactions(); // refresh logu
            statusLabel.setText("Prevod " + amount + " na účet " + targetNumber + " prebehol.");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Chyba prevodu: " +
                    e.getClass().getSimpleName() + " " + e.getMessage());
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

        double before = sel.getZostatok();
        op.accept(sel);
        double after = sel.getZostatok();

        try {
            accountDao.updateBalance(sel.getId(), sel.getZostatok());

            String opType;
            if ("Vklad prebehol".equals(okMsg)) {
                opType = "DEPOSIT";
            } else if ("Výber prebehol".equals(okMsg)) {
                opType = "WITHDRAW";
            } else {
                opType = "INTEREST";
            }

            double amount = Math.abs(after - before);
            Integer userId = (Session.get() != null) ? Session.get().getId() : null;

            transactionDao.logTransaction(
                    userId,
                    sel.getId(),
                    opType,
                    amount,
                    after,
                    null,
                    okMsg
            );

            accountsTable.refresh();
            loadUserTransactions(); // refresh logu
            statusLabel.setText(okMsg);
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Chyba ukladania: " +
                    e.getClass().getSimpleName() + " " + e.getMessage());
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
