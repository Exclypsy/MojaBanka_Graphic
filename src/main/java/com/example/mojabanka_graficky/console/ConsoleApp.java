package com.example.mojabanka_graficky.console;

import java.util.Scanner;

public class ConsoleApp {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("=== Moja Banka – konzola ===");
            System.out.println("1) Prihlásiť");
            System.out.println("0) Koniec");
            System.out.print("Voľba: ");
            String volba = sc.nextLine().trim();

            if ("0".equals(volba)) {
                System.out.println("Koniec.");
                break;
            } else if ("1".equals(volba)) {
                ConsoleLogin.handleLogin(sc);
            } else {
                System.out.println("Neznáma voľba.");
            }
        }
    }
}
