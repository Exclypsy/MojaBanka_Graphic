package com.example.mojabanka_graficky.console;

import java.util.Scanner;

/**
 * Vstupný bod konzolového režimu aplikácie MojaBanka.
 * Zobrazuje hlavné menu konzoly a umožňuje používateľovi sa prihlásiť alebo ukončiť program.
 * Tento režim je alternatívou k grafickému (JavaFX) rozhraní.
 */
public class ConsoleApp {

    /**
     * Hlavná metóda konzolového režimu.
     * V nekonečnej slučke zobrazuje menu s možnosťami:
     * <ul>
     *   <li>1 – Prihlásenie (delegované na {@link ConsoleLogin#handleLogin(Scanner)})</li>
     *   <li>0 – Ukončenie programu</li>
     * </ul>
     *
     * @param args argumenty príkazového riadka (nie sú využívané)
     */
    public static void main(String[] args) {
        // Vytvorenie Scanner-a pre čítanie vstupu z konzoly
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("=== Moja Banka – konzola ===");
            System.out.println("1) Prihlásiť");
            System.out.println("0) Koniec");
            System.out.print("Voľba: ");
            String volba = sc.nextLine().trim();

            if ("0".equals(volba)) {
                // Používateľ chce skončiť – vyskočíme zo slučky
                System.out.println("Koniec.");
                break;
            } else if ("1".equals(volba)) {
                // Spracovanie prihlásenia cez ConsoleLogin
                ConsoleLogin.handleLogin(sc);
            } else {
                System.out.println("Neznáma voľba.");
            }
        }
    }
}
