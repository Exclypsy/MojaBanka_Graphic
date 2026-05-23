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

/**
 * JavaFX kontrolér pre používateľský dashboard (user-dashboard.fxml).
 * Zobrazuje účty prihláseného používateľa, umožňuje vykonávať bankové operácie
 * (vklad, výber, úrok, prevod) a zobrazuje históriu transakcií.
 */
public class UserDashboardController {

    // ===== Tabuľka účtov =====

    /** Tabuľka zobrazujúca účty prihláseného používateľa. */
    @FXML private TableView<Ucet> accountsTable;

    /** Stĺpec s menom majiteľa účtu. */
    @FXML private TableColumn<Ucet, String> colOwner;

    /** Stĺpec s číslom účtu. */
    @FXML private TableColumn<Ucet, Number> colNumber;

    /** Stĺpec so zostatkom účtu. */
    @FXML private TableColumn<Ucet, Number> colBalance;

    /** Stĺpec s úrokom účtu. */
    @FXML private TableColumn<Ucet, Number> colInterest;

    /** Stĺpec s typom účtu (STANDARD/OVERDRAFT). */
    @FXML private TableColumn<Ucet, String> colType;

    // ===== Formulár operácií =====

    /** Pole pre zadanie sumy pri vklade alebo výbere. */
    @FXML private TextField amountField;

    /** Pole pre zadanie čísla cieľového účtu pri prevode. */
    @FXML private TextField transferTargetField;

    /** Pole pre zadanie sumy prevodu. */
    @FXML private TextField transferAmountField;

    // ===== Stavové labely =====

    /** Label na zobrazenie výsledku poslednej operácie alebo chybovej správy. */
    @FXML private Label statusLabel;

    /** Label s menom prihláseného používateľa. */
    @FXML private Label userLabel;

    // ===== Tabuľka transakcií =====

    /** Tabuľka zobrazujúca históriu transakcií prihláseného používateľa. */
    @FXML private TableView<TransactionUserView> transactionsTable;

    /** Stĺpec s dátumom a časom transakcie. */
    @FXML private TableColumn<TransactionUserView, String> colTrCreated;

    /** Stĺpec s číslom účtu v transakcii. */
    @FXML private TableColumn<TransactionUserView, String> colTrAccount;

    /** Stĺpec s typom operácie (DEPOSIT, WITHDRAW, atď.). */
    @FXML private TableColumn<TransactionUserView, String> colTrType;

    /** Stĺpec so sumou transakcie. */
    @FXML private TableColumn<TransactionUserView, Number> colTrAmount;

    /** Stĺpec so zostatkom po transakcii. */
    @FXML private TableColumn<TransactionUserView, Number> colTrBalanceAfter;

    /** Stĺpec s číslom druhého účtu pri prevode. */
    @FXML private TableColumn<TransactionUserView, String> colTrRelatedAccount;

    /** Stĺpec s popisom transakcie. */
    @FXML private TableColumn<TransactionUserView, String> colTrDescription;

    // ===== DAO a dátové zoznamy =====

    /** DAO pre prístup k účtom v databáze. */
    private final AccountDao accountDao = new AccountDao();

    /** DAO pre prístup k transakciám v databáze. */
    private final TransactionDao transactionDao = new TransactionDao();

    /** Observable zoznam účtov – napojený na tabuľku. */
    private final ObservableList<Ucet> data = FXCollections.observableArrayList();

    /** Observable zoznam transakcií – napojený na tabuľku. */
    private final ObservableList<TransactionUserView> transactionsData = FXCollections.observableArrayList();

    /** Formát dátumu a času pre zobrazenie transakcií. */
    private final DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Inicializačná metóda volaná automaticky JavaFX po načítaní FXML.
     * Nastaví hodnoty stĺpcov tabuliek, zobrazí meno prihláseného používateľa
     * a načíta dáta z databázy.
     */
    @FXML
    public void initialize() {
        // Nastavenie tovární hodnôt pre stĺpce tabuľky účtov
        colOwner.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMajitel()));
        colNumber.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().getNumber()));
        colBalance.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getZostatok()));
        colInterest.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getUrok()));
        colType.setCellValueFactory(c -> new SimpleStringProperty(
                (c.getValue() instanceof UcetDoMinusu) ? "OVERDRAFT" : "STANDARD"
        ));

        // Zobrazenie mena prihláseného používateľa v labeli
        if (Session.get() != null) {
            userLabel.setText("Prihlásený: " + Session.get().getUsername());
        }

        // Nastavenie tovární hodnôt pre stĺpce tabuľky transakcií
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

        // Načítanie počiatočných dát z databázy
        loadData();
        loadUserTransactions();
    }

    /**
     * Načíta účty prihláseného používateľa z databázy a zobrazí ich v tabuľke.
     */
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

    /**
     * Načíta transakcie prihláseného používateľa z databázy a zobrazí ich v tabuľke.
     */
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

    /**
     * Obsluha kliknutia na tlačidlo "Vklad".
     * Vloží zadanú sumu na vybraný účet.
     */
    @FXML
    private void onDeposit() {
        double amount = parseAmount(amountField);
        if (amount <= 0) return;
        // Vykonaj vklad na vybranom účte a zaloguj DEPOSIT transakciu
        mutateSelected(acc -> acc.vklad(amount), "Vklad prebehol");
    }

    /**
     * Obsluha kliknutia na tlačidlo "Výber".
     * Vyberie zadanú sumu z vybraného účtu.
     */
    @FXML
    private void onWithdraw() {
        double amount = parseAmount(amountField);
        if (amount <= 0) return;
        // Vykonaj výber na vybranom účte a zaloguj WITHDRAW transakciu
        mutateSelected(acc -> acc.vyber(amount), "Výber prebehol");
    }

    /**
     * Obsluha kliknutia na tlačidlo "Úrok".
     * Pripíše mesačný úrok na vybraný účet.
     */
    @FXML
    private void onApplyInterest() {
        mutateSelected(Ucet::zapocitajUrok, "Úrok započítaný");
    }

    /**
     * Obsluha kliknutia na tlačidlo "Prevod".
     * Presunie zadanú sumu z vybraného účtu na cieľový účet (podľa čísla).
     * Overuje existenciu cieľového účtu, dostatočný zostatok a či ide o rôzne účty.
     */
    @FXML
    private void onTransfer() {
        // Kontrola výberu zdrojového účtu
        Ucet from = accountsTable.getSelectionModel().getSelectedItem();
        if (from == null) {
            statusLabel.setText("Vyber zdrojový účet.");
            return;
        }

        // Parsovanie čísla cieľového účtu
        long targetNumber;
        try {
            targetNumber = Long.parseLong(transferTargetField.getText().trim());
        } catch (NumberFormatException e) {
            statusLabel.setText("Zadaj platné číslo cieľového účtu.");
            return;
        }

        // Parsovanie sumy prevodu
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
            // Vyhľadanie cieľového účtu podľa čísla
            Ucet to = accountDao.findByNumber(targetNumber);
            if (to == null) {
                statusLabel.setText("Cieľový účet neexistuje.");
                return;
            }

            // Kontrola, že sa neprenáša na ten istý účet
            if (to.getId() == from.getId()) {
                statusLabel.setText("Nemôžeš poslať peniaze na ten istý účet.");
                return;
            }

            // Kontrola zostatku pre štandardný účet
            if (!(from instanceof UcetDoMinusu) && from.getZostatok() < amount) {
                statusLabel.setText("Nedostatočný zostatok.");
                return;
            }

            // Vykonanie prevodu na oboch objektoch
            from.vyber(amount);
            to.vklad(amount);

            // Uloženie nových zostatkov do DB
            accountDao.updateBalance(from.getId(), from.getZostatok());
            accountDao.updateBalance(to.getId(), to.getZostatok());

            Integer userId = (Session.get() != null) ? Session.get().getId() : null;

            // Zaloguj odosielací pohyb (debet na zdrojovom účte)
            transactionDao.logTransaction(
                    userId, from.getId(), "TRANSFER_DEBIT",
                    amount, from.getZostatok(), to.getId(),
                    "Prevod na účet " + targetNumber
            );

            // Zaloguj prijímací pohyb (kredit na cieľovom účte)
            transactionDao.logTransaction(
                    userId, to.getId(), "TRANSFER_CREDIT",
                    amount, to.getZostatok(), from.getId(),
                    "Prijatý prevod z účtu " + from.getNumber()
            );

            // Obnov zobrazenie tabuľky a transakcií
            accountsTable.refresh();
            loadUserTransactions();
            statusLabel.setText("Prevod " + amount + " na účet " + targetNumber + " prebehol.");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Chyba prevodu: " +
                    e.getClass().getSimpleName() + " " + e.getMessage());
        }
    }

    /**
     * Parsuje číselnú hodnotu zo zadaného textového poľa.
     * Pri chybnom vstupe zobrazí chybový stavový text a vráti 0.
     *
     * @param field textové pole s číselnou hodnotou
     * @return parsovaná hodnota, alebo 0 pri chybe
     */
    private double parseAmount(TextField field) {
        try {
            return Double.parseDouble(field.getText().trim());
        } catch (NumberFormatException e) {
            statusLabel.setText("Zadaj platnú sumu.");
            return 0;
        }
    }

    /**
     * Pomocná metóda na vykonanie operácie (vklad, výber, úrok) na vybranom účte.
     * Aplikuje operáciu, uloží zmenu do DB a zaloguje transakciu.
     * Typ transakcie sa určuje podľa textu {@code okMsg}.
     *
     * @param op    operácia na vykonanie (lambda prijímajúca {@link Ucet})
     * @param okMsg správa zobrazená po úspešnej operácii (určuje aj typ transakcie)
     */
    private void mutateSelected(java.util.function.Consumer<Ucet> op, String okMsg) {
        // Kontrola, či je vybraný nejaký účet
        Ucet sel = accountsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            statusLabel.setText("Vyber účet.");
            return;
        }

        double before = sel.getZostatok();
        op.accept(sel);  // Vykonaj operáciu na účte
        double after = sel.getZostatok();

        try {
            // Uloženie nového zostatku do databázy
            accountDao.updateBalance(sel.getId(), sel.getZostatok());

            // Určenie typu transakcie podľa správy
            String opType;
            if ("Vklad prebehol".equals(okMsg)) {
                opType = "DEPOSIT";
            } else if ("Výber prebehol".equals(okMsg)) {
                opType = "WITHDRAW";
            } else {
                opType = "INTEREST";
            }

            double amount = Math.abs(after - before);  // Absolútna zmena zostatku
            Integer userId = (Session.get() != null) ? Session.get().getId() : null;

            // Zaloguj transakciu do histórie
            transactionDao.logTransaction(
                    userId, sel.getId(), opType,
                    amount, after, null, okMsg
            );

            // Obnov zobrazenie tabuliek
            accountsTable.refresh();
            loadUserTransactions();
            statusLabel.setText(okMsg);
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Chyba ukladania: " +
                    e.getClass().getSimpleName() + " " + e.getMessage());
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
}
