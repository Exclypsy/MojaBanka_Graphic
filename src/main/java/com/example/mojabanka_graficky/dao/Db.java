package com.example.mojabanka_graficky.dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Db {
    private static String URL;
    private static String USER;
    private static String PASS;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Properties p = new Properties();
            try (InputStream is = Db.class.getResourceAsStream("/com/example/mojabanka_graficky/db.properties")) {
                p.load(is);
            }
            URL = p.getProperty("db.url");
            USER = p.getProperty("db.user");
            PASS = p.getProperty("db.pass");
        } catch (Exception e) {
            throw new RuntimeException("DB init failed", e);
        }
    }

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
