package com.example.mojabanka_graficky.ui.admin;

import com.example.mojabanka_graficky.dao.AccountDao;
import com.example.mojabanka_graficky.dao.UserDao;
import com.example.mojabanka_graficky.model.Ucet;
import com.example.mojabanka_graficky.model.User;
import com.example.mojabanka_graficky.model.UcetDoMinusu;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleDoubleProperty;
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

    @FXML private TextField userUsernameField;
    @FXML private PasswordField userPasswordField;
    @FXML private TextField userFullNameField;
    @FXML private ChoiceBox<String> userRoleChoice;
    @FXML private Label userCreateStatus;

    private final AccountDao accountDao = new AccountDao();
    private final ObservableList<Ucet> data = FXCollections.observableArrayList();
    private final ObservableList<User> usersData = FXCollections.observableArrayList();
    private final UserDao userDao = new UserDao();

    @FXML
    public void initialize() {
        // Tabuľka účtov
        colOwner.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMajitel()));
        colNumber.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().getNumber()));
        colBalance.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getZostatok()));
        colInterest.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getUrok()));
        colType.setCellValueFactory(c -> new SimpleStringProperty(
                (c.getValue() instanceof UcetDoMinusu) ? "OVERDRAFT" : "STANDARD"
        ));
        reload();

        // Tabuľka užívateľov
        colUserUsername.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsername()));
        colUserRole.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRole()));
        colUserFullName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        refreshUsers();

        // Roly na výber pri zakladaní užívateľa
        if (userRoleChoice != null) {
            userRoleChoice.setItems(FXCollections.observableArrayList("USER", "ADMIN"));
            userRoleChoice.setValue("USER");
        }
    }

    private void reload() {
        try {
            data.setAll(accountDao.findAll());
            accountsTable.setItems(data);
        } catch (Exception e) {
            // log error ak chceš
        }
    }

    private void refreshUsers() {
        try {
            usersData.setAll(userDao.findAll());
            usersTable.setItems(usersData);
        } catch (Exception e) {
            userCreateStatus.setText("Chyba načítania užívateľov: " + e.getMessage());
        }
    }

    @FXML
    private void onDelete() {
        Ucet sel = accountsTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        try {
            accountDao.delete(sel.getId());
            reload();
        } catch (Exception ignored) {}
    }

    @FXML
    private void onCreateUser() {
        String username = userUsernameField.getText().trim();
        String password = userPasswordField.getText();
        String fullName = userFullNameField.getText().trim();
        String role = userRoleChoice.getValue();
        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            userCreateStatus.setText("Vyplň všetky polia.");
            return;
        }
        try {
            userDao.create(username, password, role, fullName);
            userCreateStatus.setText("Používateľ vytvorený.");
            refreshUsers();
        } catch (Exception e) {
            userCreateStatus.setText("Chyba: " + e.getMessage());
        }
    }
}
