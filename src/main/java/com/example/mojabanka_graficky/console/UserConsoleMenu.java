package com.example.mojabanka_graficky.console;

import com.example.mojabanka_graficky.dao.AccountDao;
import com.example.mojabanka_graficky.dao.TransactionDao;
import com.example.mojabanka_graficky.dao.TransactionDao.TransactionUserView;
import com.example.mojabanka_graficky.model.Ucet;
import com.example.mojabanka_graficky.model.UcetDoMinusu;
import com.example.mojabanka_graficky.security.Session;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Konzolové menu pre bežného (rola USER) prihláseného používateľa.
 * Umožňuje zobraziť účty, vykonávať vklady, výbery, úroky, prevody
 * a zobraziť históriu vlastných transakcií.
 */
public class UserConsoleMenu {

    /** DAO pre prístup k účtom v databáze. */
    private static final AccountDao accountDao = new AccountDao();

    /** DAO pre prístup k transakciám v databáze. */
    private static final TransactionDao transactionDao = new TransactionDao();

    /** Formát dátumu a času pre výpis transakcií. */
    private static final DateTimeFormatter dtFmt =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Zobrazí hlavné menu používateľa a spracúva jeho voľby v slučke
     * dovtedy, kým nezvolí odhlásenie (0).
     *
     * @param sc Scanner na čítanie vstupu z konzoly
     */
    public static void show(Scanner sc) {
        while (true) {
            System.out.println("\n=== User menu (" + Session.get().getUsername() + ") ===");
            System.out.println("1) Zobraziť účty");
            System.out.println("2) Vklad");
            System.out.println("3) Výber");
            System.out.println("4) Započítať úrok");
            System.out.println("5) Prevod");
            System.out.println("6) Moje transakcie");
            System.out.println("0) Odhlásiť");
            System.out.print("Voľba: ");
            String v = sc.nextLine().trim();

            try {
                switch (v) {
                    case "1" -> showAccounts();
                    case "2" -> deposit(sc);
                    case "3" -> withdraw(sc);
                    case "4" -> applyInterest(sc);
                    case "5" -> transfer(sc);
                    case "6" -> showTransactions();
                    case "0" -> { return; }
                    default -> System.out.println("Neznáma voľba.");
                }
            } catch (Exception e) {
                System.out.println("Chyba: " + e.getMessage());
            }
        }
    }

    /**
     * Vráti zoznam všetkých účtov aktuálne prihláseného používateľa z databázy.
     *
     * @return zoznam účtov
     * @throws Exception ak nastane DB chyba
     */
    private static List<Ucet> userAccounts() throws Exception {
        return accountDao.findByUserId(Session.get().getId());
    }

    /**
     * Vypíše zoznam účtov a nechá používateľa vybrať jeden z nich.
     * Vracia null, ak používateľ nemá účty alebo zadá neplatnú voľbu.
     *
     * @param sc Scanner na čítanie vstupu
     * @return vybraný účet, alebo null
     * @throws Exception ak nastane DB chyba
     */
    private static Ucet chooseAccount(Scanner sc) throws Exception {
        List<Ucet> accs = userAccounts();
        if (accs.isEmpty()) {
            System.out.println("Nemáš žiadne účty.");
            return null;
        }
        // Výpis očíslovaného zoznamu účtov
        for (int i = 0; i < accs.size(); i++) {
            Ucet a = accs.get(i);
            String typ = (a instanceof UcetDoMinusu) ? "OVERDRAFT" : "STANDARD";
            System.out.printf("%d) %s / %d / zostatok: %.2f / %s%n",
                    i + 1, a.getMajitel(), a.getNumber(), a.getZostatok(), typ);
        }
        System.out.print("Vyber účet (číslo): ");
        int idx = Integer.parseInt(sc.nextLine().trim()) - 1;
        if (idx < 0 || idx >= accs.size()) {
            System.out.println("Neplatná voľba.");
            return null;
        }
        return accs.get(idx);
    }

    /**
     * Vypíše všetky účty prihláseného používateľa aj s detailmi
     * (číslo, majiteľ, zostatok, úrok, typ).
     *
     * @throws Exception ak nastane DB chyba
     */
    private static void showAccounts() throws Exception {
        System.out.println("\n-- Moje účty --");
        for (Ucet a : userAccounts()) {
            String typ = (a instanceof UcetDoMinusu) ? "OVERDRAFT" : "STANDARD";
            System.out.printf("%d / majiteľ: %s / zostatok: %.2f / úrok: %.2f / %s%n",
                    a.getNumber(), a.getMajitel(), a.getZostatok(), a.getUrok(), typ);
        }
    }

    /**
     * Spracuje vklad peňazí na vybraný účet.
     * Suma musí byť kladná. Aktualizuje zostatok v DB a zaloguje DEPOSIT transakciu.
     *
     * @param sc Scanner na čítanie vstupu
     * @throws Exception ak nastane DB chyba
     */
    private static void deposit(Scanner sc) throws Exception {
        Ucet a = chooseAccount(sc);
        if (a == null) return;

        System.out.print("Suma vkladu: ");
        double amount = Double.parseDouble(sc.nextLine().trim());
        if (amount <= 0) {
            System.out.println("Suma musí byť > 0.");
            return;
        }

        // Vykonaj vklad na objekte a ulož do DB
        a.vklad(amount);
        accountDao.updateBalance(a.getId(), a.getZostatok());

        // Zaloguj transakciu
        transactionDao.logTransaction(
                Session.get().getId(), a.getId(), "DEPOSIT",
                amount, a.getZostatok(), null, "Vklad cez konzolu"
        );
        System.out.printf("Vklad prebehol. Nový zostatok: %.2f%n", a.getZostatok());
    }

    /**
     * Spracuje výber peňazí z vybraného účtu.
     * Pre štandardný účet skontroluje dostatočný zostatok.
     * Aktualizuje zostatok v DB a zaloguje WITHDRAW transakciu.
     *
     * @param sc Scanner na čítanie vstupu
     * @throws Exception ak nastane DB chyba
     */
    private static void withdraw(Scanner sc) throws Exception {
        Ucet a = chooseAccount(sc);
        if (a == null) return;

        System.out.print("Suma výberu: ");
        double amount = Double.parseDouble(sc.nextLine().trim());
        if (amount <= 0) {
            System.out.println("Suma musí byť > 0.");
            return;
        }
        // Kontrola zostatku iba pre štandardný účet
        if (!(a instanceof UcetDoMinusu) && a.getZostatok() < amount) {
            System.out.println("Nedostatočný zostatok.");
            return;
        }

        // Vykonaj výber a ulož do DB
        a.vyber(amount);
        accountDao.updateBalance(a.getId(), a.getZostatok());

        // Zaloguj transakciu
        transactionDao.logTransaction(
                Session.get().getId(), a.getId(), "WITHDRAW",
                amount, a.getZostatok(), null, "Výber cez konzolu"
        );
        System.out.printf("Výber prebehol. Nový zostatok: %.2f%n", a.getZostatok());
    }

    /**
     * Pripíše úrok na vybraný účet a zaloguje INTEREST transakciu.
     * Vypočíta rozdiel zostatku pred a po ako sumu transakcie.
     *
     * @param sc Scanner na čítanie vstupu
     * @throws Exception ak nastane DB chyba
     */
    private static void applyInterest(Scanner sc) throws Exception {
        Ucet a = chooseAccount(sc);
        if (a == null) return;

        double before = a.getZostatok();
        a.zapocitajUrok();
        double after = a.getZostatok();
        double diff = Math.abs(after - before);

        accountDao.updateBalance(a.getId(), after);
        transactionDao.logTransaction(
                Session.get().getId(), a.getId(), "INTEREST",
                diff, after, null, "Započítanie úroku (konzola)"
        );
        System.out.printf("Úrok započítaný. Prírastok: %.2f, nový zostatok: %.2f%n", diff, after);
    }

    /**
     * Spracuje bankový prevod z vybraného účtu na cieľový účet (podľa čísla).
     * Zaloguje TRANSFER_DEBIT na zdrojovom a TRANSFER_CREDIT na cieľovom účte.
     *
     * @param sc Scanner na čítanie vstupu
     * @throws Exception ak nastane DB chyba
     */
    private static void transfer(Scanner sc) throws Exception {
        Ucet from = chooseAccount(sc);
        if (from == null) return;

        System.out.print("Cieľové číslo účtu: ");
        long targetNumber = Long.parseLong(sc.nextLine().trim());

        System.out.print("Suma: ");
        double amount = Double.parseDouble(sc.nextLine().trim());
        if (amount <= 0) {
            System.out.println("Suma musí byť > 0.");
            return;
        }

        // Vyhľadanie cieľového účtu podľa čísla
        Ucet to = accountDao.findByNumber(targetNumber);
        if (to == null) {
            System.out.println("Cieľový účet neexistuje.");
            return;
        }

        // Kontrola zostatku pre štandardný účet
        if (!(from instanceof UcetDoMinusu) && from.getZostatok() < amount) {
            System.out.println("Nedostatočný zostatok.");
            return;
        }

        // Vykonaj prevod na oboch objektoch
        from.vyber(amount);
        to.vklad(amount);

        // Ulož nové zostatky do DB
        accountDao.updateBalance(from.getId(), from.getZostatok());
        accountDao.updateBalance(to.getId(), to.getZostatok());

        Integer uid = Session.get().getId();

        // Zaloguj odosielací pohyb (debet)
        transactionDao.logTransaction(
                uid, from.getId(), "TRANSFER_DEBIT",
                amount, from.getZostatok(), to.getId(),
                "Prevod na účet " + targetNumber + " (konzola)"
        );
        // Zaloguj prijímací pohyb (kredit)
        transactionDao.logTransaction(
                uid, to.getId(), "TRANSFER_CREDIT",
                amount, to.getZostatok(), from.getId(),
                "Prijatý prevod z účtu " + from.getNumber() + " (konzola)"
        );

        System.out.println("Prevod prebehol.");
    }

    /**
     * Vypíše históriu všetkých transakcií prihláseného používateľa
     * zo všetkých jeho účtov, zotriedených od najnovšej.
     *
     * @throws Exception ak nastane DB chyba
     */
    private static void showTransactions() throws Exception {
        System.out.println("\n-- Moje transakcie --");
        for (TransactionUserView t : transactionDao.findForUser(Session.get().getId())) {
            System.out.printf("%s | účet %s | %s | %.2f | po: %.2f | druhý účet: %s | %s%n",
                    t.createdAt().format(dtFmt),
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
