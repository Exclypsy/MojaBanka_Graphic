package com.example.mojabanka_graficky.ui.admin;

import com.example.mojabanka_graficky.HelloApplication;
import com.example.mojabanka_graficky.dao.AccountDao;
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
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AdminDashboardController {

    @FXML private TableView<Ucet> accountsTable;
    @FXML private TableColumn<Ucet, String> colOwner;
    @FXML private TableColumn<Ucet, Number> colNumber;
    @FXML private TableColumn<Ucet, Number> colBalance;
    @FXML private TableColumn<Ucet, Number> colInterest;
    @FXML private TableColumn<Ucet, String> colType;

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> colUserUsername;
    @FXML private TableColumn<User, String> colUserRole;
    @FXML private TableColumn<User, String> colUserFullName;

    // editácia účtu
    @FXML private TextField editOwnerField;
    @FXML private TextField editNumberField;
    @FXML private TextField editBalanceField;
    @FXML private TextField editInterestField;
    @FXML private ChoiceBox<String> editTypeChoice;
    @FXML private TextField editOverdraftLimitField;
    @FXML private TextField editOverdraftInterestField;

    // vytvorenie používateľa + účtu
    @FXML private TextField userUsernameField;
    @FXML private PasswordField userPasswordField;
    @FXML private TextField userFullNameField;
    @FXML private ChoiceBox<String> userRoleChoice;

    @FXML private TextField newAccountNumberField;
    @FXML private TextField newAccountBalanceField;
    @FXML private TextField newAccountInterestField;
    @FXML private ChoiceBox<String> newAccountTypeChoice;
    @FXML private TextField newAccountOverdraftLimitField;
    @FXML private TextField newAccountOverdraftInterestField;

    @FXML private Label userCreateStatus;
    @FXML private Label globalStatus;

    private final AccountDao accountDao = new AccountDao();
    private final UserDao userDao = new UserDao();
    private final ObservableList<Ucet> data = FXCollections.observableArrayList();
    private final ObservableList<User> usersData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // tabuľka účtov
        colOwner.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMajitel()));
        colNumber.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().getNumber()));
        colBalance.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getZostatok()));
        colInterest.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getUrok()));
        colType.setCellValueFactory(c -> new SimpleStringProperty(
                (c.getValue() instanceof UcetDoMinusu) ? "OVERDRAFT" : "STANDARD"
        ));
        reloadAccounts();

        // tabuľka používateľov
        colUserUsername.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsername()));
        colUserRole.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRole()));
        colUserFullName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        reloadUsers();

        // role
        if (userRoleChoice != null) {
            userRoleChoice.setItems(FXCollections.observableArrayList("USER", "ADMIN"));
            userRoleChoice.setValue("USER");
        }

        // typy účtov
        if (editTypeChoice != null) {
            editTypeChoice.setItems(FXCollections.observableArrayList("STANDARD", "OVERDRAFT"));
        }
        if (newAccountTypeChoice != null) {
            newAccountTypeChoice.setItems(FXCollections.observableArrayList("STANDARD", "OVERDRAFT"));
            newAccountTypeChoice.setValue("STANDARD");
        }

        // naplnenie edit formulára po kliknutí na účet
        accountsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, sel) -> {
            if (sel != null) {
                fillEditForm(sel);
            }
        });
    }

    private void reloadAccounts() {
        try {
            data.setAll(accountDao.findAll());
            accountsTable.setItems(data);
            globalStatus.setText("");
        } catch (Exception e) {
            globalStatus.setText("Chyba načítania účtov: " + e.getMessage());
        }
    }

    private void reloadUsers() {
        try {
            usersData.setAll(userDao.findAll());
            usersTable.setItems(usersData);
        } catch (Exception e) {
            userCreateStatus.setText("Chyba načítania používateľov: " + e.getMessage());
        }
    }

    private void fillEditForm(Ucet acc) {
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
        Ucet sel = accountsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            globalStatus.setText("Vyber účet na zmazanie.");
            return;
        }
        try {
            accountDao.delete(sel.getId());
            reloadAccounts();
        } catch (Exception e) {
            globalStatus.setText("Chyba mazania: " + e.getMessage());
        }
    }

    @FXML
    private void onUpdateAccount() {
        Ucet sel = accountsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
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

            accountDao.updateAccount(sel.getId(), owner, number, balance, interest, type, limit, odInt);
            reloadAccounts();
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

        String strNumber = newAccountNumberField.getText().trim();
        String strBalance = newAccountBalanceField.getText().trim();
        String strInterest = newAccountInterestField.getText().trim();
        String type = newAccountTypeChoice.getValue();
        String strLimit = newAccountOverdraftLimitField.getText().trim();
        String strOdInt = newAccountOverdraftInterestField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || role == null ||
                strNumber.isEmpty() || strBalance.isEmpty() || strInterest.isEmpty() || type == null) {
            userCreateStatus.setText("Vyplň všetky povinné polia používateľa aj účtu.");
            return;
        }

        try {
            // 1) vytvor používateľa
            int userId = userDao.createAndReturnId(username, password, role, fullName);

            long number = Long.parseLong(strNumber);
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

            userCreateStatus.setText("Používateľ a účet vytvorený.");
            reloadUsers();
            reloadAccounts();
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
}
