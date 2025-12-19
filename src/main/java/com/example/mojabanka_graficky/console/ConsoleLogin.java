package com.example.mojabanka_graficky.console;

import com.example.mojabanka_graficky.dao.UserDao;
import com.example.mojabanka_graficky.model.User;
import com.example.mojabanka_graficky.security.Session;

import java.util.Scanner;

public class ConsoleLogin {

    private static final UserDao userDao = new UserDao();

    public static void handleLogin(Scanner sc) {
        try {
            System.out.print("Username: ");
            String username = sc.nextLine().trim();
            System.out.print("Heslo: ");
            String password = sc.nextLine().trim();

            User u = userDao.findByUsernameAndPassword(username, password);
            if (u == null) {
                System.out.println("Nesprávne meno alebo heslo.");
                return;
            }

            Session.set(u);

            if ("ADMIN".equalsIgnoreCase(u.getRole())) {
                AdminConsoleMenu.show(sc);
            } else {
                UserConsoleMenu.show(sc);
            }

            Session.clear();
        } catch (Exception e) {
            System.out.println("Chyba loginu: " + e.getMessage());
        }
    }
}
