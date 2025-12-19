package com.example.mojabanka_graficky.ui.admin;

import com.example.mojabanka_graficky.HelloApplication;
import com.example.mojabanka_graficky.dao.AccountDao;
import com.example.mojabanka_graficky.dao.TransactionDao;
import com.example.mojabanka_graficky.dao.UserDao;
import com.example.mojabanka_graficky.model.Ucet;
import com.example.mojabanka_graficky.model.UcetDoMinusu;
import com.example.mojabanka_graficky.model.User;
import com.example.mojabanka_graficky.security.Session;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.format.DateTimeFormatter;

public class AdminDashboardController {

    // tabuľka účtov + používateľov (joinované v modeli)
    @FXML private TableView<UcetWrapper> accountsTable;
    @FXML private TableColumn<UcetWrapper, String> colUserUsername;
    @FXML private TableColumn<UcetWrapper, String> colUserFullName;
    @FXML private TableColumn<UcetWrapper, String> colUserRole;
    @FXML private TableColumn<UcetWrapper, String> colOwner;
    @FXML private TableColumn<UcetWrapper, Number> colNumber;
    @FXML private TableColumn<UcetWrapper, Number> colBalance;
    @FXML private TableColumn<UcetWrapper, Number> colInterest;
    @FXML private TableColumn<UcetWrapper, String> colType;
    @FXML private TableColumn<UcetWrapper, Number> colLimit;
    @FXML private TableColumn<UcetWrapper, Number> colOverdraftInterest;

    // filtre / search
    @FXML private TextField searchField;
    @FXML private ChoiceBox<String> filterTypeChoice;
    @FXML private ChoiceBox<String> filterRoleChoice;
    @FXML private CheckBox showAdminAccountsCheck;

    // editácia účtu
    @FXML private TextField editOwnerField;
    @FXML private TextField editNumberField;
    @FXML private TextField editBalanceField;
    @FXML private TextField editInterestField;
    @FXML private ChoiceBox<String> editTypeChoice;
    @FXML private TextField editOverdraftLimitField;
    @FXML private TextField editOverdraftInterestField;

    // nový používateľ + účet
    @FXML private TextField userUsernameField;
    @FXML private PasswordField userPasswordField;
    @FXML private TextField userFullNameField;
    @FXML private ChoiceBox<String> userRoleChoice;

    @FXML private TextField newAccountBalanceField;
    @FXML private TextField newAccountInterestField;
    @FXML private ChoiceBox<String> newAccountTypeChoice;
    @FXML private TextField newAccountOverdraftLimitField;
    @FXML private TextField newAccountOverdraftInterestField;

    @FXML private Label userCreateStatus;
    @FXML private Label globalStatus;

    // tabuľka transakcií
    @FXML private TableView<TransactionView> transactionsTable;
    @FXML private TableColumn<TransactionView, String> colTrCreated;
    @FXML private TableColumn<TransactionView, String> colTrUser;
    @FXML private TableColumn<TransactionView, String> colTrAccount;
    @FXML private TableColumn<TransactionView, String> colTrType;
    @FXML private TableColumn<TransactionView, Number> colTrAmount;
    @FXML private TableColumn<TransactionView, Number> colTrBalanceAfter;
    @FXML private TableColumn<TransactionView, String> colTrRelatedAccount;
    @FXML private TableColumn<TransactionView, String> colTrDescription;

    private final AccountDao accountDao = new AccountDao();
    private final UserDao userDao = new UserDao();
    private final TransactionDao transactionDao = new TransactionDao();

    private final ObservableList<UcetWrapper> masterData = FXCollections.observableArrayList();
    private FilteredList<UcetWrapper> filteredData;

    private final ObservableList<TransactionView> transactionsData = FXCollections.observableArrayList();

    private final DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        // stĺpce – account môže byť null
        colUserUsername.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getUser().getUsername()));
        colUserFullName.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getUser().getFullName()));
        colUserRole.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getUser().getRole()));

        colOwner.setCellValueFactory(c -> {
            Ucet acc = c.getValue().getAccount();
            return new SimpleStringProperty(acc != null ? acc.getMajitel() : "");
        });
        colNumber.setCellValueFactory(c -> {
            Ucet acc = c.getValue().getAccount();
            return new SimpleLongProperty(acc != null ? acc.getNumber() : 0L);
        });
        colBalance.setCellValueFactory(c -> {
            Ucet acc = c.getValue().getAccount();
            return new SimpleDoubleProperty(acc != null ? acc.getZostatok() : 0.0);
        });
        colInterest.setCellValueFactory(c -> {
            Ucet acc = c.getValue().getAccount();
            return new SimpleDoubleProperty(acc != null ? acc.getUrok() : 0.0);
        });
        colType.setCellValueFactory(c -> {
            Ucet acc = c.getValue().getAccount();
            String type = "";
            if (acc != null) {
                type = (acc instanceof UcetDoMinusu) ? "OVERDRAFT" : "STANDARD";
            }
            return new SimpleStringProperty(type);
        });
        colLimit.setCellValueFactory(c -> {
            Ucet acc = c.getValue().getAccount();
            if (acc instanceof UcetDoMinusu odm) {
                return new SimpleDoubleProperty(odm.getPovolenePrecerpanie());
            }
            return new SimpleDoubleProperty(0.0);
        });
        colOverdraftInterest.setCellValueFactory(c -> {
            Ucet acc = c.getValue().getAccount();
            if (acc instanceof UcetDoMinusu odm) {
                return new SimpleDoubleProperty(odm.getUrokDoMinusu());
            }
            return new SimpleDoubleProperty(0.0);
        });

        // role a typy – defaulty
        userRoleChoice.setItems(FXCollections.observableArrayList("USER", "ADMIN"));
        userRoleChoice.setValue("USER");

        editTypeChoice.setItems(FXCollections.observableArrayList("STANDARD", "OVERDRAFT"));
        newAccountTypeChoice.setItems(FXCollections.observableArrayList("STANDARD", "OVERDRAFT"));
        newAccountTypeChoice.setValue("STANDARD");

        filterTypeChoice.setItems(FXCollections.observableArrayList("Všetky", "STANDARD", "OVERDRAFT"));
        filterTypeChoice.setValue("Všetky");
        filterRoleChoice.setItems(FXCollections.observableArrayList("Všetky", "USER", "ADMIN"));
        filterRoleChoice.setValue("Všetky");

        showAdminAccountsCheck.setSelected(false);

        // data + FilteredList
        reloadData();
        filteredData = new FilteredList<>(masterData, x -> true);
        accountsTable.setItems(filteredData);
        applyFilters();

        // listeners na filtre
        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        filterTypeChoice.valueProperty().addListener((obs, o, n) -> applyFilters());
        filterRoleChoice.valueProperty().addListener((obs, o, n) -> applyFilters());
        showAdminAccountsCheck.selectedProperty().addListener((obs, o, n) -> applyFilters());

        // selection → formulár
        accountsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, sel) -> {
            if (sel != null && sel.getAccount() != null) {
                fillEditForm(sel);
            }
        });

        // transakcie – stĺpce
        colTrCreated.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().createdAt().format(dtFmt)));
        colTrUser.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().username()));
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

        loadTransactions();
    }

    private void reloadData() {
        try {
            masterData.clear();
            var users = userDao.findAll();
            for (User u : users) {
                var accounts = accountDao.findByUserId(u.getId());
                if (accounts.isEmpty()) {
                    masterData.add(new UcetWrapper(u, null));
                } else {
                    for (Ucet acc : accounts) {
                        masterData.add(new UcetWrapper(u, acc));
                    }
                }
            }
            globalStatus.setText("");

            if (filteredData != null) {
                applyFilters();
            }
        } catch (Exception e) {
            globalStatus.setText("Chyba načítania dát: " + e.getMessage());
        }
    }

    private void loadTransactions() {
        try {
            transactionsData.setAll(transactionDao.findAllForAdmin());
            transactionsTable.setItems(transactionsData);
        } catch (Exception e) {
            globalStatus.setText("Chyba načítania logu transakcií: " + e.getMessage());
        }
    }

    private void applyFilters() {
        if (filteredData == null) return;

        String text = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String typeFilter = filterTypeChoice.getValue();
        String roleFilter = filterRoleChoice.getValue();
        boolean showAdmin = showAdminAccountsCheck.isSelected();

        if (typeFilter == null) typeFilter = "Všetky";
        if (roleFilter == null) roleFilter = "Všetky";

        String finalTypeFilter = typeFilter;
        String finalRoleFilter = roleFilter;

        filteredData.setPredicate(w -> {
            User u = w.getUser();
            Ucet acc = w.getAccount();

            boolean matchesText = text.isEmpty()
                    || u.getUsername().toLowerCase().contains(text)
                    || u.getFullName().toLowerCase().contains(text)
                    || (acc != null && String.valueOf(acc.getNumber()).contains(text));
            if (!matchesText) return false;

            String type = "";
            if (acc != null) {
                type = (acc instanceof UcetDoMinusu) ? "OVERDRAFT" : "STANDARD";
            }
            if (!"Všetky".equals(finalTypeFilter)) {
                if (acc == null) return false;
                if (!finalTypeFilter.equals(type)) return false;
            }

            String role = u.getRole();
            if (!"Všetky".equals(finalRoleFilter) && !finalRoleFilter.equals(role)) return false;

            if (!showAdmin && "ADMIN".equals(role)) return false;

            return true;
        });
    }

    @FXML
    private void onClearFilters() {
        searchField.clear();
        filterTypeChoice.setValue("Všetky");
        filterRoleChoice.setValue("Všetky");
        showAdminAccountsCheck.setSelected(false);
    }

    private void fillEditForm(UcetWrapper wrap) {
        Ucet acc = wrap.getAccount();
        if (acc == null) {
            editOwnerField.clear();
            editNumberField.clear();
            editBalanceField.clear();
            editInterestField.clear();
            editTypeChoice.setValue("STANDARD");
            editOverdraftLimitField.clear();
            editOverdraftInterestField.clear();
            return;
        }

        editOwnerField.setText(acc.getMajitel());
        editNumberField.setText(String.valueOf(acc.getNumber()));
        editBalanceField.setText(String.valueOf(acc.getZostatok()));
        editInterestField.setText(String.valueOf(acc.getUrok()));
        if (acc instanceof UcetDoMinusu odm) {
            editTypeChoice.setValue("OVERDRAFT");
            editOverdraftLimitField.setText(String.valueOf(odm.getPovolenePrecerpanie()));
            editOverdraftInterestField.setText(String.valueOf(odm.getUrokDoMinusu()));
        } else {
            editTypeChoice.setValue("STANDARD");
            editOverdraftLimitField.clear();
            editOverdraftInterestField.clear();
        }
    }

    @FXML
    private void onDeleteAccount() {
        UcetWrapper sel = accountsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            globalStatus.setText("Vyber riadok na zmazanie.");
            return;
        }

        User u = sel.getUser();
        Ucet acc = sel.getAccount();

        try {
            if (acc != null) {
                accountDao.delete(acc.getId());
                globalStatus.setText("Účet zmazaný.");
            } else {
                userDao.delete(u.getId());
                globalStatus.setText("Používateľ zmazaný.");
            }
            reloadData();
        } catch (Exception e) {
            globalStatus.setText("Chyba mazania: " + e.getMessage());
        }
    }

    @FXML
    private void onUpdateAccount() {
        UcetWrapper sel = accountsTable.getSelectionModel().getSelectedItem();
        if (sel == null || sel.getAccount() == null) {
            globalStatus.setText("Vyber účet na úpravu.");
            return;
        }
        try {
            String owner = editOwnerField.getText().trim();
            long number = Long.parseLong(editNumberField.getText().trim());
            double balance = Double.parseDouble(editBalanceField.getText().trim());
            double interest = Double.parseDouble(editInterestField.getText().trim());
            String type = editTypeChoice.getValue();

            Double limit = null;
            Double odInt = null;
            if ("OVERDRAFT".equals(type)) {
                limit = Double.parseDouble(editOverdraftLimitField.getText().trim());
                odInt = Double.parseDouble(editOverdraftInterestField.getText().trim());
            }

            accountDao.updateAccount(sel.getAccount().getId(), owner, number, balance, interest, type, limit, odInt);
            reloadData();
            globalStatus.setText("Účet upravený.");
        } catch (Exception e) {
            globalStatus.setText("Chyba úpravy účtu: " + e.getMessage());
        }
    }

    @FXML
    private void onCreateUserWithAccount() {
        String username = userUsernameField.getText().trim();
        String password = userPasswordField.getText();
        String fullName = userFullNameField.getText().trim();
        String role = userRoleChoice.getValue();

        String strBalance  = newAccountBalanceField.getText().trim();
        String strInterest = newAccountInterestField.getText().trim();
        String type        = newAccountTypeChoice.getValue();
        String strLimit    = newAccountOverdraftLimitField.getText().trim();
        String strOdInt    = newAccountOverdraftInterestField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || role == null) {
            userCreateStatus.setText("Vyplň všetky polia používateľa.");
            return;
        }

        try {
            int userId = userDao.createAndReturnId(username, password, role, fullName);

            if (strBalance.isEmpty() && strInterest.isEmpty() && type == null) {
                userCreateStatus.setText("Používateľ vytvorený bez účtu.");
                reloadData();
                return;
            }

            if (strBalance.isEmpty() || strInterest.isEmpty() || type == null) {
                userCreateStatus.setText("Pre účet vyplň zostatok, úrok a typ.");
                return;
            }

            long number = accountDao.generateNextAccountNumber();
            double balance = Double.parseDouble(strBalance);
            double interest = Double.parseDouble(strInterest);

            if ("OVERDRAFT".equals(type)) {
                if (strLimit.isEmpty() || strOdInt.isEmpty()) {
                    userCreateStatus.setText("Pre OVERDRAFT vyplň limit aj úrok mínus.");
                    return;
                }
                double limit = Double.parseDouble(strLimit);
                double odInt = Double.parseDouble(strOdInt);
                accountDao.createOverdraft(userId, fullName, number, balance, interest, limit, odInt);
            } else {
                accountDao.createStandard(userId, fullName, number, balance, interest);
            }

            userCreateStatus.setText("Používateľ a účet vytvorený. Číslo účtu: " + number);
            reloadData();
        } catch (Exception e) {
            userCreateStatus.setText("Chyba vytvárania: " + e.getMessage());
        }
    }

    @FXML
    private void onLogout() {
        try {
            Session.clear();
            HelloApplication.setRoot("login-view");
        } catch (Exception ignored) {}
    }

    public static class UcetWrapper {
        private final User user;
        private final Ucet account;

        public UcetWrapper(User user, Ucet account) {
            this.user = user;
            this.account = account;
        }
        public User getUser() { return user; }
        public Ucet getAccount() { return account; }
    }

    // jednoduchý view objekt na zobrazenie transakcií
    public record TransactionView(
            long id,
            java.time.LocalDateTime createdAt,
            String username,
            String accountNumber,
            String operationType,
            double amount,
            double balanceAfter,
            String relatedAccountNumber,
            String description
    ) {}
}
