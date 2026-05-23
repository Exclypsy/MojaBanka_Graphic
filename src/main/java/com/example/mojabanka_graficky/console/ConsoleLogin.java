package com.example.mojabanka_graficky.console;

import com.example.mojabanka_graficky.dao.UserDao;
import com.example.mojabanka_graficky.model.User;
import com.example.mojabanka_graficky.security.Session;

import java.util.Scanner;

/**
 * Trieda zodpovedná za prihlasovanie používateľa v konzolom režime.
 * Po úspešnom prihlásení presmeruje do správneho menu
 * ({@link AdminConsoleMenu} pre admina, {@link UserConsoleMenu} pre bežného používateľa).
 */
public class ConsoleLogin {

    /** DAO pre prístup k databáze používateľov. */
    private static final UserDao userDao = new UserDao();

    /**
     * Spracuje prihlásenie používateľa.
     * Prečíta username a heslo zo vstupu, overí ich v databáze
     * a uloží prihláseného používateľa do {@link Session}.
     * Po odhlásení (návrat z menu) session vyčistí.
     *
     * @param sc Scanner na čítanie vstupu z konzoly
     */
    public static void handleLogin(Scanner sc) {
        try {
            // Načítanie prihlasovacích údajov od používateľa
            System.out.print("Username: ");
            String username = sc.nextLine().trim();
            System.out.print("Heslo: ");
            String password = sc.nextLine().trim();

            // Overenie používateľa voči databáze
            User u = userDao.findByUsernameAndPassword(username, password);
            if (u == null) {
                // Nesprávne prihlasovacie údaje
                System.out.println("Nesprávne meno alebo heslo.");
                return;
            }

            // Uloženie prihláseného používateľa do session
            Session.set(u);

            // Presmerovanie do správneho menu podľa roly
            if ("ADMIN".equalsIgnoreCase(u.getRole())) {
                AdminConsoleMenu.show(sc);
            } else {
                UserConsoleMenu.show(sc);
            }

            // Po odhlásení (návrate z menu) vyčistiť session
            Session.clear();
        } catch (Exception e) {
            System.out.println("Chyba loginu: " + e.getMessage());
        }
    }
}
