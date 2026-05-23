package com.example.mojabanka_graficky.console;

import com.example.mojabanka_graficky.dao.AccountDao;
import com.example.mojabanka_graficky.dao.TransactionDao;
import com.example.mojabanka_graficky.dao.TransactionDao.TransactionUserView;
import com.example.mojabanka_graficky.dao.UserDao;
import com.example.mojabanka_graficky.model.Ucet;
import com.example.mojabanka_graficky.model.UcetDoMinusu;
import com.example.mojabanka_graficky.model.User;
import com.example.mojabanka_graficky.ui.admin.AdminDashboardController;
import com.example.mojabanka_graficky.ui.admin.AdminDashboardController.TransactionView;
import com.example.mojabanka_graficky.security.Session;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Konzolové menu pre administrátora (rola ADMIN).
 * Umožňuje spravovať používateľov a účty, upravovať a mazať záznamy
 * a prezerať celý log transakcií v systéme.
 */
public class AdminConsoleMenu {

    /** DAO pre prístup k používateľom v databáze. */
    private static final UserDao userDao = new UserDao();

    /** DAO pre prístup k účtom v databáze. */
    private static final AccountDao accountDao = new AccountDao();

    /** DAO pre prístup k transakciám v databáze. */
    private static final TransactionDao transactionDao = new TransactionDao();

    /** Formát dátumu a času pre výpis transakcií. */
    private static final DateTimeFormatter dtFmt =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Zobrazí hlavné admin menu a spracúva voľby v slučke
     * dovtedy, kým admin nezvolí odhlásenie (0).
     *
     * @param sc Scanner na čítanie vstupu z konzoly
     */
    public static void show(Scanner sc) {
        while (true) {
            // Zobrazenie menu s menom prihláseného admina
            System.out.println("\n=== Admin menu (" + Session.get().getUsername() + ") ===");
            System.out.println("1) Zobraziť používateľov + účty");
            System.out.println("2) Vytvoriť používateľa + účet");
            System.out.println("3) Upraviť účet");
            System.out.println("4) Zmazať účet / používateľa");
            System.out.println("5) Všetky transakcie (log)");
            System.out.println("0) Odhlásiť");
            System.out.print("Voľba: ");
            String v = sc.nextLine().trim();

            try {
                switch (v) {
                    case "1" -> showUsersAndAccounts();
                    case "2" -> createUserWithAccount(sc);
                    case "3" -> updateAccount(sc);
                    case "4" -> deleteAccountOrUser(sc);
                    case "5" -> showAllTransactions();
                    case "0" -> { return; }
                    default -> System.out.println("Neznáma voľba.");
                }
            } catch (Exception e) {
                System.out.println("Chyba: " + e.getMessage());
            }
        }
    }

    /**
     * Vypíše zoznam všetkých používateľov spolu s ich účtami.
     * Pre každý účet zobrazí typ (STANDARD/OVERDRAFT) a prípadné detaily prečerpania.
     *
     * @throws Exception ak nastane chyba pri prístupe k databáze
     */
    private static void showUsersAndAccounts() throws Exception {
        System.out.println("\n-- Používatelia a účty --");
        List<User> users = userDao.findAll();
        for (User u : users) {
            // Vypíš základné info o používateľovi
            System.out.printf("User ID=%d, username=%s, rola=%s, meno=%s%n",
                    u.getId(), u.getUsername(), u.getRole(), u.getFullName());
            List<Ucet> accounts = accountDao.findByUserId(u.getId());
            if (accounts.isEmpty()) {
                System.out.println("   (bez účtov)");
            } else {
                for (Ucet a : accounts) {
                    String typ = (a instanceof UcetDoMinusu) ? "OVERDRAFT" : "STANDARD";
                    String extra = "";
                    // Pre OVERDRAFT účet pridaj info o limite a úroku z prečerpania
                    if (a instanceof UcetDoMinusu odm) {
                        extra = String.format(" / limit: %.2f / urok_minus: %.2f",
                                odm.getPovolenePrecerpanie(), odm.getUrokDoMinusu());
                    }
                    System.out.printf("   Účet ID=%d, číslo=%d, majiteľ=%s, zostatok=%.2f, úrok=%.2f, typ=%s%s%n",
                            a.getId(), a.getNumber(), a.getMajitel(),
                            a.getZostatok(), a.getUrok(), typ, extra);
                }
            }
        }
    }

    /**
     * Vytvorí nového používateľa a voliteľne aj bankový účet.
     * Pýta si username, heslo, celé meno, rolu a parametre účtu (zostatok, úrok, typ).
     * Pre OVERDRAFT účet pýta aj limit a úrok v mínuse.
     *
     * @param sc Scanner na čítanie vstupu z konzoly
     * @throws Exception ak nastane chyba pri prístupe k databáze
     */
    private static void createUserWithAccount(Scanner sc) throws Exception {
        System.out.println("\n-- Vytvoriť používateľa + účet --");

        // Načítanie základných údajov používateľa
        System.out.print("Username: ");
        String username = sc.nextLine().trim();
        System.out.print("Heslo: ");
        String password = sc.nextLine().trim();
        System.out.print("Celé meno: ");
        String fullName = sc.nextLine().trim();
        System.out.print("Rola (USER/ADMIN): ");
        String role = sc.nextLine().trim().toUpperCase();

        // Validácia povinných polí
        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()
                || (!"USER".equals(role) && !"ADMIN".equals(role))) {
            System.out.println("Chyba: vyplň všetky údaje a správnu rolu.");
            return;
        }

        // Načítanie parametrov účtu (voliteľné)
        System.out.print("Počiatočný zostatok (prázdne = bez účtu): ");
        String sBalance = sc.nextLine().trim();

        System.out.print("Úrok % p.a. (ak účet): ");
        String sInterest = sc.nextLine().trim();

        System.out.print("Typ účtu (STANDARD/OVERDRAFT, prázdne = bez účtu): ");
        String type = sc.nextLine().trim().toUpperCase();
        if (type.isEmpty()) type = null;

        // Vytvorenie používateľa v DB a získanie jeho ID
        int userId = userDao.createAndReturnId(username, password, role, fullName);

        // Ak neboli zadané žiadne údaje o účte, vytvor len používateľa
        if (sBalance.isEmpty() && sInterest.isEmpty() && type == null) {
            System.out.println("Používateľ vytvorený bez účtu, ID=" + userId);
            return;
        }

        // Ak boli zadané len niektoré polia účtu, upozorni
        if (sBalance.isEmpty() || sInterest.isEmpty() || type == null) {
            System.out.println("Pre účet musíš vyplniť zostatok, úrok a typ.");
            return;
        }

        // Generovanie nového čísla účtu a parsovanie hodnôt
        long number = accountDao.generateNextAccountNumber();
        double balance = Double.parseDouble(sBalance);
        double interest = Double.parseDouble(sInterest);

        if ("OVERDRAFT".equals(type)) {
            // Pre OVERDRAFT pýtaj navyše limit a úrok z prečerpania
            System.out.print("Limit (OVERDRAFT): ");
            double limit = Double.parseDouble(sc.nextLine().trim());
            System.out.print("Úrok v mínuse (%): ");
            double odInt = Double.parseDouble(sc.nextLine().trim());
            accountDao.createOverdraft(userId, fullName, number, balance, interest, limit, odInt);
        } else {
            accountDao.createStandard(userId, fullName, number, balance, interest);
        }

        System.out.println("Používateľ a účet vytvorený. User ID=" + userId +
                ", číslo účtu=" + number);
    }

    /**
     * Umožní adminovi upraviť existujúci účet (podľa ID účtu).
     * Ponúka zmenu majiteľa, čísla, zostatku, úroku a typu účtu.
     * Pre OVERDRAFT typ pýta aj limit a úrok v mínuse.
     *
     * @param sc Scanner na čítanie vstupu z konzoly
     * @throws Exception ak nastane chyba pri prístupe k databáze
     */
    private static void updateAccount(Scanner sc) throws Exception {
        System.out.println("\n-- Úprava účtu --");
        System.out.print("Zadaj ID účtu: ");
        int accId = Integer.parseInt(sc.nextLine().trim());

        // Načítanie účtu podľa ID
        Ucet acc = accountDao.findById(accId);
        if (acc == null) {
            System.out.println("Účet s týmto ID neexistuje.");
            return;
        }

        String typ = (acc instanceof UcetDoMinusu) ? "OVERDRAFT" : "STANDARD";
        // Zobrazenie aktuálnych hodnôt účtu pred úpravou
        System.out.printf("Aktuálne: majiteľ=%s, číslo=%d, zostatok=%.2f, úrok=%.2f, typ=%s%n",
                acc.getMajitel(), acc.getNumber(), acc.getZostatok(), acc.getUrok(), typ);

        // Načítanie nových hodnôt – prázdny vstup = ponechaj pôvodnú hodnotu
        System.out.print("Nový majiteľ (enter = ponechať): ");
        String owner = sc.nextLine().trim();
        if (owner.isEmpty()) owner = acc.getMajitel();

        System.out.print("Nové číslo účtu (enter = ponechať): ");
        String sNumber = sc.nextLine().trim();
        long number = sNumber.isEmpty() ? acc.getNumber() : Long.parseLong(sNumber);

        System.out.print("Nový zostatok (enter = ponechať): ");
        String sBal = sc.nextLine().trim();
        double balance = sBal.isEmpty() ? acc.getZostatok() : Double.parseDouble(sBal);

        System.out.print("Nový úrok (enter = ponechať): ");
        String sInt = sc.nextLine().trim();
        double interest = sInt.isEmpty() ? acc.getUrok() : Double.parseDouble(sInt);

        System.out.print("Typ účtu (STANDARD/OVERDRAFT, enter = " + typ + "): ");
        String newType = sc.nextLine().trim().toUpperCase();
        if (newType.isEmpty()) newType = typ;

        Double limit = null;
        Double odInt = null;
        if ("OVERDRAFT".equals(newType)) {
            // Pre OVERDRAFT typ načítaj aktuálne hodnoty ako predvolené
            double currentLimit = (acc instanceof UcetDoMinusu odm) ? odm.getPovolenePrecerpanie() : 0.0;
            double currentOdInt = (acc instanceof UcetDoMinusu odm) ? odm.getUrokDoMinusu() : 0.0;

            System.out.print("Limit (enter = " + currentLimit + "): ");
            String sLim = sc.nextLine().trim();
            limit = sLim.isEmpty() ? currentLimit : Double.parseDouble(sLim);

            System.out.print("Úrok v mínuse (enter = " + currentOdInt + "): ");
            String sOd = sc.nextLine().trim();
            odInt = sOd.isEmpty() ? currentOdInt : Double.parseDouble(sOd);
        }

        // Uloženie zmien do databázy
        accountDao.updateAccount(accId, owner, number, balance, interest, newType, limit, odInt);
        System.out.println("Účet upravený.");
    }

    /**
     * Zmaže účet alebo používateľa z databázy.
     * Ponúka dve možnosti:
     * <ul>
     *   <li>Zmazanie účtu podľa ID účtu</li>
     *   <li>Zmazanie používateľa podľa ID používateľa</li>
     * </ul>
     *
     * @param sc Scanner na čítanie vstupu z konzoly
     * @throws Exception ak nastane chyba pri prístupe k databáze
     */
    private static void deleteAccountOrUser(Scanner sc) throws Exception {
        System.out.println("\n-- Zmazanie účtu / používateľa --");
        System.out.println("1) Zmazať účet podľa ID");
        System.out.println("2) Zmazať používateľa podľa ID (bez účtov alebo po ručnom zmazaní účtov)");
        System.out.print("Voľba: ");
        String v = sc.nextLine().trim();

        switch (v) {
            case "1" -> {
                System.out.print("ID účtu: ");
                int accId = Integer.parseInt(sc.nextLine().trim());
                accountDao.delete(accId);
                System.out.println("Účet zmazaný.");
            }
            case "2" -> {
                System.out.print("ID používateľa: ");
                int userId = Integer.parseInt(sc.nextLine().trim());
                userDao.delete(userId);
                System.out.println("Používateľ zmazaný.");
            }
            default -> System.out.println("Neznáma voľba.");
        }
    }

    /**
     * Vypíše všetky transakcie v systéme (log pre admina).
     * Zoradené od najnovšej. Obsahuje info o používateľovi, účte,
     * type operácie, sume, zostatku po a prípadnom druhom účte.
     *
     * @throws Exception ak nastane chyba pri prístupe k databáze
     */
    private static void showAllTransactions() throws Exception {
        System.out.println("\n-- Všetky transakcie --");
        List<TransactionView> list = transactionDao.findAllForAdmin();
        for (TransactionView t : list) {
            System.out.printf("%s | user=%s | účet=%s | %s | %.2f | po: %.2f | druhý účet: %s | %s%n",
                    t.createdAt().format(dtFmt),
                    t.username(),
                    t.accountNumber(),
                    t.operationType(),
                    t.amount(),
                    t.balanceAfter(),
                    t.relatedAccountNumber(),
                    t.description()
            );
        }
    }
}
