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

/**
 * JavaFX kontrolér pre admin dashboard (admin-dashboard.fxml).
 * Zobrazuje zoznam všetkých používateľov a účtov, umožňuje ich filtrovanie,
 * editáciu, mazanie a vytváranie nových. Zobrazuje aj celý log transakcií.
 */
public class AdminDashboardController {

    // ===== Tabuľka účtov (joinovaná s používateľmi) =====

    /** Tabuľka zobrazujúca používateľov spolu s ich účtami (ako UcetWrapper objekty). */
    @FXML private TableView<UcetWrapper> accountsTable;

    /** Stĺpec s prihlasovacím menom používateľa. */
    @FXML private TableColumn<UcetWrapper, String> colUserUsername;

    /** Stĺpec s celým menom používateľa. */
    @FXML private TableColumn<UcetWrapper, String> colUserFullName;

    /** Stĺpec s rolou používateľa (USER/ADMIN). */
    @FXML private TableColumn<UcetWrapper, String> colUserRole;

    /** Stĺpec s menom majiteľa účtu. */
    @FXML private TableColumn<UcetWrapper, String> colOwner;

    /** Stĺpec s číslom účtu. */
    @FXML private TableColumn<UcetWrapper, Number> colNumber;

    /** Stĺpec so zostatkom účtu. */
    @FXML private TableColumn<UcetWrapper, Number> colBalance;

    /** Stĺpec s úrokom účtu. */
    @FXML private TableColumn<UcetWrapper, Number> colInterest;

    /** Stĺpec s typom účtu (STANDARD/OVERDRAFT). */
    @FXML private TableColumn<UcetWrapper, String> colType;

    /** Stĺpec s limitom prečerpania (iba pre OVERDRAFT). */
    @FXML private TableColumn<UcetWrapper, Number> colLimit;

    /** Stĺpec s úrokom z prečerpania (iba pre OVERDRAFT). */
    @FXML private TableColumn<UcetWrapper, Number> colOverdraftInterest;

    // ===== Filtre a vyhľadávanie =====

    /** Textové pole na vyhľadávanie podľa mena alebo čísla účtu. */
    @FXML private TextField searchField;

    /** Výber filtra podľa typu účtu (Všetky/STANDARD/OVERDRAFT). */
    @FXML private ChoiceBox<String> filterTypeChoice;

    /** Výber filtra podľa roly používateľa (Všetky/USER/ADMIN). */
    @FXML private ChoiceBox<String> filterRoleChoice;

    /** Zaškrtávacie pole – ak zaškrtnuté, zobrazí aj admin účty. */
    @FXML private CheckBox showAdminAccountsCheck;

    // ===== Editačný formulár účtu =====

    /** Pole pre editáciu mena majiteľa účtu. */
    @FXML private TextField editOwnerField;

    /** Pole pre editáciu čísla účtu. */
    @FXML private TextField editNumberField;

    /** Pole pre editáciu zostatku účtu. */
    @FXML private TextField editBalanceField;

    /** Pole pre editáciu úroku účtu. */
    @FXML private TextField editInterestField;

    /** Výber nového typu účtu pri editácii. */
    @FXML private ChoiceBox<String> editTypeChoice;

    /** Pole pre editáciu limitu prečerpania (iba OVERDRAFT). */
    @FXML private TextField editOverdraftLimitField;

    /** Pole pre editáciu úroku z prečerpania (iba OVERDRAFT). */
    @FXML private TextField editOverdraftInterestField;

    // ===== Formulár vytvorenia nového používateľa a účtu =====

    /** Pole pre username nového používateľa. */
    @FXML private TextField userUsernameField;

    /** Pole pre heslo nového používateľa. */
    @FXML private PasswordField userPasswordField;

    /** Pole pre celé meno nového používateľa. */
    @FXML private TextField userFullNameField;

    /** Výber roly nového používateľa (USER/ADMIN). */
    @FXML private ChoiceBox<String> userRoleChoice;

    /** Pole pre počiatočný zostatok nového účtu. */
    @FXML private TextField newAccountBalanceField;

    /** Pole pre úrok nového účtu. */
    @FXML private TextField newAccountInterestField;

    /** Výber typu nového účtu (STANDARD/OVERDRAFT). */
    @FXML private ChoiceBox<String> newAccountTypeChoice;

    /** Pole pre limit prečerpania nového OVERDRAFT účtu. */
    @FXML private TextField newAccountOverdraftLimitField;

    /** Pole pre úrok z prečerpania nového OVERDRAFT účtu. */
    @FXML private TextField newAccountOverdraftInterestField;

    /** Label na zobrazenie výsledku vytvorenia používateľa/účtu. */
    @FXML private Label userCreateStatus;

    /** Label na zobrazenie globálnych stavových a chybových správ. */
    @FXML private Label globalStatus;

    // ===== Tabuľka transakcií =====

    /** Tabuľka zobrazujúca všetky transakcie v systéme (admin pohľad). */
    @FXML private TableView<TransactionView> transactionsTable;

    /** Stĺpec s dátumom a časom transakcie. */
    @FXML private TableColumn<TransactionView, String> colTrCreated;

    /** Stĺpec s používateľom, ktorý transakciu vykonal. */
    @FXML private TableColumn<TransactionView, String> colTrUser;

    /** Stĺpec s číslom účtu v transakcii. */
    @FXML private TableColumn<TransactionView, String> colTrAccount;

    /** Stĺpec s typom operácie. */
    @FXML private TableColumn<TransactionView, String> colTrType;

    /** Stĺpec so sumou transakcie. */
    @FXML private TableColumn<TransactionView, Number> colTrAmount;

    /** Stĺpec so zostatkom po transakcii. */
    @FXML private TableColumn<TransactionView, Number> colTrBalanceAfter;

    /** Stĺpec s číslom druhého účtu pri prevode. */
    @FXML private TableColumn<TransactionView, String> colTrRelatedAccount;

    /** Stĺpec s popisom transakcie. */
    @FXML private TableColumn<TransactionView, String> colTrDescription;

    // ===== DAO objekty =====

    /** DAO pre prístup k účtom v databáze. */
    private final AccountDao accountDao = new AccountDao();

    /** DAO pre prístup k používateľom v databáze. */
    private final UserDao userDao = new UserDao();

    /** DAO pre prístup k transakciám v databáze. */
    private final TransactionDao transactionDao = new TransactionDao();

    // ===== Dátové zoznamy =====

    /** Hlavný zoznam všetkých záznamov (používateľ + účet) – podklad pre filtrovanie. */
    private final ObservableList<UcetWrapper> masterData = FXCollections.observableArrayList();

    /** Filtrovaný pohľad na masterData – zobrazovaný v tabuľke. */
    private FilteredList<UcetWrapper> filteredData;

    /** Observable zoznam transakcií napojený na tabuľku transakcií. */
    private final ObservableList<TransactionView> transactionsData = FXCollections.observableArrayList();

    /** Formát dátumu a času pre zobrazenie transakcií. */
    private final DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Inicializačná metóda volaná automaticky JavaFX po načítaní FXML.
     * Nastaví stĺpce oboch tabuliek, inicializuje filtre a načíta dáta z databázy.
     */
    @FXML
    public void initialize() {
        // Nastavenie tovární hodnôt pre stĺpce tabuľky účtov
        // Stĺpce používateľa
        colUserUsername.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getUser().getUsername()));
        colUserFullName.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getUser().getFullName()));
        colUserRole.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getUser().getRole()));

        // Stĺpce účtu (account môže byť null ak používateľ nemá účet)
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

        // Inicializácia ChoiceBoxov s hodnotami
        userRoleChoice.setItems(FXCollections.observableArrayList("USER", "ADMIN"));
        userRoleChoice.setValue("USER");

        editTypeChoice.setItems(FXCollections.observableArrayList("STANDARD", "OVERDRAFT"));
        newAccountTypeChoice.setItems(FXCollections.observableArrayList("STANDARD", "OVERDRAFT"));
        newAccountTypeChoice.setValue("STANDARD");

        filterTypeChoice.setItems(FXCollections.observableArrayList("Všetky", "STANDARD", "OVERDRAFT"));
        filterTypeChoice.setValue("Všetky");
        filterRoleChoice.setItems(FXCollections.observableArrayList("Všetky", "USER", "ADMIN"));
        filterRoleChoice.setValue("Všetky");

        // Admin účty sú štandardne skryté
        showAdminAccountsCheck.setSelected(false);

        // Načítanie dát a nastavenie filtrovaného pohľadu
        reloadData();
        filteredData = new FilteredList<>(masterData, x -> true);
        accountsTable.setItems(filteredData);
        applyFilters();

        // Listener na zmeny filtrov – automaticky prefiltruje tabuľku
        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        filterTypeChoice.valueProperty().addListener((obs, o, n) -> applyFilters());
        filterRoleChoice.valueProperty().addListener((obs, o, n) -> applyFilters());
        showAdminAccountsCheck.selectedProperty().addListener((obs, o, n) -> applyFilters());

        // Listener na výber riadku – vyplní editačný formulár hodnotami vybraného účtu
        accountsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, sel) -> {
            if (sel != null && sel.getAccount() != null) {
                fillEditForm(sel);
            }
        });

        // Nastavenie stĺpcov tabuľky transakcií
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

        // Načítanie histórie transakcií
        loadTransactions();
    }

    /**
     * Znovu načíta všetkých používateľov a ich účty z databázy do masterData.
     * Používateľ bez účtu je reprezentovaný ako UcetWrapper(user, null).
     */
    private void reloadData() {
        try {
            masterData.clear();
            var users = userDao.findAll();
            for (User u : users) {
                var accounts = accountDao.findByUserId(u.getId());
                if (accounts.isEmpty()) {
                    // Používateľ bez účtu – pridaj riadok s null účtom
                    masterData.add(new UcetWrapper(u, null));
                } else {
                    // Pre každý účet vytvor samostatný riadok
                    for (Ucet acc : accounts) {
                        masterData.add(new UcetWrapper(u, acc));
                    }
                }
            }
            globalStatus.setText("");

            // Ak je FilteredList inicializovaný, obnov filtre
            if (filteredData != null) {
                applyFilters();
            }
        } catch (Exception e) {
            globalStatus.setText("Chyba načítania dát: " + e.getMessage());
        }
    }

    /**
     * Načíta všetky transakcie z databázy a zobrazí ich v tabuľke transakcií.
     */
    private void loadTransactions() {
        try {
            transactionsData.setAll(transactionDao.findAllForAdmin());
            transactionsTable.setItems(transactionsData);
        } catch (Exception e) {
            globalStatus.setText("Chyba načítania logu transakcií: " + e.getMessage());
        }
    }

    /**
     * Aplikuje aktívne filtre (text, typ účtu, rola, zobrazenie admina) na tabuľku.
     * Nastaví predikát pre FilteredList, ktorý filtruje záznamy podľa zadaných kritérií.
     */
    private void applyFilters() {
        if (filteredData == null) return;

        // Načítanie aktuálnych hodnôt filtrov
        String text = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String typeFilter = filterTypeChoice.getValue();
        String roleFilter = filterRoleChoice.getValue();
        boolean showAdmin = showAdminAccountsCheck.isSelected();

        if (typeFilter == null) typeFilter = "Všetky";
        if (roleFilter == null) roleFilter = "Všetky";

        String finalTypeFilter = typeFilter;
        String finalRoleFilter = roleFilter;

        // Nastavenie predikátu pre filtrovanie
        filteredData.setPredicate(w -> {
            User u = w.getUser();
            Ucet acc = w.getAccount();

            // Filter podľa textu – porovnáva s menom používateľa, celým menom a číslom účtu
            boolean matchesText = text.isEmpty()
                    || u.getUsername().toLowerCase().contains(text)
                    || u.getFullName().toLowerCase().contains(text)
                    || (acc != null && String.valueOf(acc.getNumber()).contains(text));
            if (!matchesText) return false;

            // Filter podľa typu účtu
            String type = "";
            if (acc != null) {
                type = (acc instanceof UcetDoMinusu) ? "OVERDRAFT" : "STANDARD";
            }
            if (!"Všetky".equals(finalTypeFilter)) {
                if (acc == null) return false;
                if (!finalTypeFilter.equals(type)) return false;
            }

            // Filter podľa roly
            String role = u.getRole();
            if (!"Všetky".equals(finalRoleFilter) && !finalRoleFilter.equals(role)) return false;

            // Skrytie admin účtov ak nie je zaškrtnuté
            if (!showAdmin && "ADMIN".equals(role)) return false;

            return true;
        });
    }

    /**
     * Obsluha kliknutia na tlačidlo "Zrušiť filtre".
     * Resetuje všetky filtre na predvolené hodnoty.
     */
    @FXML
    private void onClearFilters() {
        searchField.clear();
        filterTypeChoice.setValue("Všetky");
        filterRoleChoice.setValue("Všetky");
        showAdminAccountsCheck.setSelected(false);
    }

    /**
     * Vyplní editačný formulár hodnotami z vybraného záznamu v tabuľke.
     * Ak účet je null (používateľ bez účtu), formulár sa vyčistí.
     *
     * @param wrap vybraný wrapper (používateľ + účet)
     */
    private void fillEditForm(UcetWrapper wrap) {
        Ucet acc = wrap.getAccount();
        if (acc == null) {
            // Používateľ nemá účet – vymaž formulár
            editOwnerField.clear();
            editNumberField.clear();
            editBalanceField.clear();
            editInterestField.clear();
            editTypeChoice.setValue("STANDARD");
            editOverdraftLimitField.clear();
            editOverdraftInterestField.clear();
            return;
        }

        // Vyplnenie formulára aktuálnymi hodnotami účtu
        editOwnerField.setText(acc.getMajitel());
        editNumberField.setText(String.valueOf(acc.getNumber()));
        editBalanceField.setText(String.valueOf(acc.getZostatok()));
        editInterestField.setText(String.valueOf(acc.getUrok()));
        if (acc instanceof UcetDoMinusu odm) {
            // Pre OVERDRAFT vyplň aj špeciálne polia
            editTypeChoice.setValue("OVERDRAFT");
            editOverdraftLimitField.setText(String.valueOf(odm.getPovolenePrecerpanie()));
            editOverdraftInterestField.setText(String.valueOf(odm.getUrokDoMinusu()));
        } else {
            editTypeChoice.setValue("STANDARD");
            editOverdraftLimitField.clear();
            editOverdraftInterestField.clear();
        }
    }

    /**
     * Obsluha kliknutia na tlačidlo "Zmazať".
     * Zmaže vybraný účet, alebo ak nemá účet – zmaže celého používateľa.
     */
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
                // Zmazanie iba účtu (používateľ zostáva)
                accountDao.delete(acc.getId());
                globalStatus.setText("Účet zmazaný.");
            } else {
                // Zmazanie celého používateľa (bez účtu)
                userDao.delete(u.getId());
                globalStatus.setText("Používateľ zmazaný.");
            }
            reloadData();
        } catch (Exception e) {
            globalStatus.setText("Chyba mazania: " + e.getMessage());
        }
    }

    /**
     * Obsluha kliknutia na tlačidlo "Uložiť zmeny" v editačnom formulári.
     * Overí, že je vybraný účet, načíta hodnoty z formulára a uloží ich do DB.
     */
    @FXML
    private void onUpdateAccount() {
        UcetWrapper sel = accountsTable.getSelectionModel().getSelectedItem();
        if (sel == null || sel.getAccount() == null) {
            globalStatus.setText("Vyber účet na úpravu.");
            return;
        }
        try {
            // Načítanie hodnôt z editačného formulára
            String owner = editOwnerField.getText().trim();
            long number = Long.parseLong(editNumberField.getText().trim());
            double balance = Double.parseDouble(editBalanceField.getText().trim());
            double interest = Double.parseDouble(editInterestField.getText().trim());
            String type = editTypeChoice.getValue();

            // Pre OVERDRAFT načítaj aj špeciálne polia
            Double limit = null;
            Double odInt = null;
            if ("OVERDRAFT".equals(type)) {
                limit = Double.parseDouble(editOverdraftLimitField.getText().trim());
                odInt = Double.parseDouble(editOverdraftInterestField.getText().trim());
            }

            // Uloženie zmien do databázy
            accountDao.updateAccount(sel.getAccount().getId(), owner, number, balance, interest, type, limit, odInt);
            reloadData();
            globalStatus.setText("Účet upravený.");
        } catch (Exception e) {
            globalStatus.setText("Chyba úpravy účtu: " + e.getMessage());
        }
    }

    /**
     * Obsluha kliknutia na tlačidlo "Vytvoriť používateľa".
     * Overí polia formulára, vytvorí nového používateľa a voliteľne aj jeho bankový účet.
     */
    @FXML
    private void onCreateUserWithAccount() {
        // Načítanie hodnôt z formulára nového používateľa
        String username = userUsernameField.getText().trim();
        String password = userPasswordField.getText();
        String fullName = userFullNameField.getText().trim();
        String role = userRoleChoice.getValue();

        String strBalance  = newAccountBalanceField.getText().trim();
        String strInterest = newAccountInterestField.getText().trim();
        String type        = newAccountTypeChoice.getValue();
        String strLimit    = newAccountOverdraftLimitField.getText().trim();
        String strOdInt    = newAccountOverdraftInterestField.getText().trim();

        // Validácia povinných polí používateľa
        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || role == null) {
            userCreateStatus.setText("Vyplň všetky polia používateľa.");
            return;
        }

        try {
            // Vytvorenie používateľa v DB
            int userId = userDao.createAndReturnId(username, password, role, fullName);

            // Ak neboli zadané polia účtu, vytvor len používateľa
            if (strBalance.isEmpty() && strInterest.isEmpty() && type == null) {
                userCreateStatus.setText("Používateľ vytvorený bez účtu.");
                reloadData();
                return;
            }

            // Ak sú zadané iba niektoré polia účtu, upozorni
            if (strBalance.isEmpty() || strInterest.isEmpty() || type == null) {
                userCreateStatus.setText("Pre účet vyplň zostatok, úrok a typ.");
                return;
            }

            // Generovanie čísla účtu a parsovanie hodnôt
            long number = accountDao.generateNextAccountNumber();
            double balance = Double.parseDouble(strBalance);
            double interest = Double.parseDouble(strInterest);

            if ("OVERDRAFT".equals(type)) {
                // Pre OVERDRAFT overenie a parsovanie špeciálnych polí
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

    /**
     * Obsluha kliknutia na tlačidlo "Odhlásiť".
     * Vyčistí session a presmeruje späť na prihlasovaciu obrazovku.
     */
    @FXML
    private void onLogout() {
        try {
            Session.clear();
            HelloApplication.setRoot("login-view");
        } catch (Exception ignored) {}
    }

    // ===== Vnorené triedy a záznamy =====

    /**
     * Pomocná wrapper trieda spájajúca objekt používateľa a jeho účtu.
     * Používa sa pre riadky v tabuľke admina (jeden riadok = jeden účet jedného používateľa).
     * Ak používateľ nemá účet, {@code account} je null.
     */
    public static class UcetWrapper {

        /** Používateľ tohto záznamu. */
        private final User user;

        /** Účet tohto záznamu – môže byť null ak používateľ nemá účet. */
        private final Ucet account;

        /**
         * Vytvorí nový wrapper so zadaným používateľom a účtom.
         *
         * @param user    vlastník záznamu
         * @param account účet vlastníka (alebo null)
         */
        public UcetWrapper(User user, Ucet account) {
            this.user = user;
            this.account = account;
        }

        /** @return používateľ tohto záznamu */
        public User getUser() { return user; }

        /** @return účet tohto záznamu, alebo null */
        public Ucet getAccount() { return account; }
    }

    /**
     * Read-only záznamový objekt (record) pre zobrazenie transakcie v admin tabuľke.
     * Obsahuje všetky stĺpce vrátane mena používateľa.
     *
     * @param id                   ID transakcie
     * @param createdAt            dátum a čas vytvorenia
     * @param username             meno používateľa, ktorý transakciu vykonal
     * @param accountNumber        číslo účtu
     * @param operationType        typ operácie (DEPOSIT, WITHDRAW, atď.)
     * @param amount               suma transakcie
     * @param balanceAfter         zostatok po transakcii
     * @param relatedAccountNumber číslo druhého účtu pri prevode (alebo null)
     * @param description          popis transakcie
     */
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
