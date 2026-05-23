package com.example.mojabanka_graficky.dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Pomocná trieda na správu databázového pripojenia.
 * Načíta konfiguráciu z resources súboru {@code db.properties}
 * a poskytuje metódu na získanie JDBC spojenia s MySQL databázou.
 *
 * <p>Súbor {@code db.properties} musí obsahovať kľúče:
 * <ul>
 *   <li>{@code db.url} – JDBC URL databázy (napr. {@code jdbc:mysql://localhost:3306/mojabanka})</li>
 *   <li>{@code db.user} – meno databázového používateľa</li>
 *   <li>{@code db.pass} – heslo databázového používateľa</li>
 * </ul>
 */
public class Db {

    /** JDBC URL pre pripojenie k databáze – načítané z db.properties. */
    private static String URL;

    /** Meno databázového používateľa – načítané z db.properties. */
    private static String USER;

    /** Heslo databázového používateľa – načítané z db.properties. */
    private static String PASS;

    // Statický inicializačný blok – spustí sa raz pri prvom použití triedy
    static {
        try {
            // Načítanie MySQL JDBC drivera
            Class.forName("com.mysql.cj.jdbc.Driver");
            Properties p = new Properties();
            // Načítanie konfiguračného súboru z classpath (resources)
            try (InputStream is = Db.class.getResourceAsStream("/com/example/mojabanka_graficky/db.properties")) {
                p.load(is);
            }
            URL  = p.getProperty("db.url");
            USER = p.getProperty("db.user");
            PASS = p.getProperty("db.pass");
        } catch (Exception e) {
            throw new RuntimeException("DB init failed", e);
        }
    }

    /**
     * Vytvorí a vráti nové JDBC pripojenie k databáze.
     * Volajúci kód je zodpovedný za uzavretie spojenia (napr. pomocou try-with-resources).
     *
     * @return aktívne {@link Connection} pripojenie k MySQL databáze
     * @throws SQLException ak sa pripojenie nepodarí vytvoriť
     */
    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
