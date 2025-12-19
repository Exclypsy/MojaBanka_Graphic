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

public class UserConsoleMenu {

    private static final AccountDao accountDao = new AccountDao();
    private static final TransactionDao transactionDao = new TransactionDao();
    private static final DateTimeFormatter dtFmt =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

    private static List<Ucet> userAccounts() throws Exception {
        return accountDao.findByUserId(Session.get().getId());
    }

    private static Ucet chooseAccount(Scanner sc) throws Exception {
        List<Ucet> accs = userAccounts();
        if (accs.isEmpty()) {
            System.out.println("Nemáš žiadne účty.");
            return null;
        }
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

    private static void showAccounts() throws Exception {
        System.out.println("\n-- Moje účty --");
        for (Ucet a : userAccounts()) {
            String typ = (a instanceof UcetDoMinusu) ? "OVERDRAFT" : "STANDARD";
            System.out.printf("%d / majiteľ: %s / zostatok: %.2f / úrok: %.2f / %s%n",
                    a.getNumber(), a.getMajitel(), a.getZostatok(), a.getUrok(), typ);
        }
    }

    private static void deposit(Scanner sc) throws Exception {
        Ucet a = chooseAccount(sc);
        if (a == null) return;

        System.out.print("Suma vkladu: ");
        double amount = Double.parseDouble(sc.nextLine().trim());
        if (amount <= 0) {
            System.out.println("Suma musí byť > 0.");
            return;
        }

        a.vklad(amount);
        accountDao.updateBalance(a.getId(), a.getZostatok());

        transactionDao.logTransaction(
                Session.get().getId(),
                a.getId(),
                "DEPOSIT",
                amount,
                a.getZostatok(),
                null,
                "Vklad cez konzolu"
        );
        System.out.printf("Vklad prebehol. Nový zostatok: %.2f%n", a.getZostatok());
    }

    private static void withdraw(Scanner sc) throws Exception {
        Ucet a = chooseAccount(sc);
        if (a == null) return;

        System.out.print("Suma výberu: ");
        double amount = Double.parseDouble(sc.nextLine().trim());
        if (amount <= 0) {
            System.out.println("Suma musí byť > 0.");
            return;
        }
        if (!(a instanceof UcetDoMinusu) && a.getZostatok() < amount) {
            System.out.println("Nedostatočný zostatok.");
            return;
        }

        a.vyber(amount);
        accountDao.updateBalance(a.getId(), a.getZostatok());

        transactionDao.logTransaction(
                Session.get().getId(),
                a.getId(),
                "WITHDRAW",
                amount,
                a.getZostatok(),
                null,
                "Výber cez konzolu"
        );
        System.out.printf("Výber prebehol. Nový zostatok: %.2f%n", a.getZostatok());
    }

    private static void applyInterest(Scanner sc) throws Exception {
        Ucet a = chooseAccount(sc);
        if (a == null) return;

        double before = a.getZostatok();
        a.zapocitajUrok();
        double after = a.getZostatok();
        double diff = Math.abs(after - before);

        accountDao.updateBalance(a.getId(), after);
        transactionDao.logTransaction(
                Session.get().getId(),
                a.getId(),
                "INTEREST",
                diff,
                after,
                null,
                "Započítanie úroku (konzola)"
        );
        System.out.printf("Úrok započítaný. Prírastok: %.2f, nový zostatok: %.2f%n", diff, after);
    }

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

        Ucet to = accountDao.findByNumber(targetNumber);
        if (to == null) {
            System.out.println("Cieľový účet neexistuje.");
            return;
        }

        if (!(from instanceof UcetDoMinusu) && from.getZostatok() < amount) {
            System.out.println("Nedostatočný zostatok.");
            return;
        }

        from.vyber(amount);
        to.vklad(amount);

        accountDao.updateBalance(from.getId(), from.getZostatok());
        accountDao.updateBalance(to.getId(), to.getZostatok());

        Integer uid = Session.get().getId();

        transactionDao.logTransaction(
                uid, from.getId(), "TRANSFER_DEBIT",
                amount, from.getZostatok(), to.getId(),
                "Prevod na účet " + targetNumber + " (konzola)"
        );
        transactionDao.logTransaction(
                uid, to.getId(), "TRANSFER_CREDIT",
                amount, to.getZostatok(), from.getId(),
                "Prijatý prevod z účtu " + from.getNumber() + " (konzola)"
        );

        System.out.println("Prevod prebehol.");
    }

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
