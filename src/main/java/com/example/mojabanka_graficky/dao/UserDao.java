package com.example.mojabanka_graficky.dao;

import com.example.mojabanka_graficky.model.User;

import java.sql.*;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) trieda pre operácie s používateľmi (tabuľka {@code users}).
 * Poskytuje metódy na vyhľadávanie, vytváranie a mazanie používateľov v databáze.
 *
 * <p><b>Poznámka:</b> Heslá sú momentálne ukladané ako plain text v stĺpci {@code password_hash}.
 * V produkcii ich treba nahradiť bezpečným hashom (napr. BCrypt).
 */
public class UserDao {

    /**
     * Vyhľadá používateľa podľa jeho používateľského mena.
     * Používa sa pri prihlasovaní cez GUI (AuthService).
     *
     * @param username hľadané používateľské meno
     * @return {@link Optional} s nájdeným používateľom, alebo prázdny Optional ak neexistuje
     * @throws SQLException ak nastane chyba pri prístupe k databáze
     */
    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, role, full_name FROM users WHERE username = ?";
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        }
    }

    /**
     * Vyhľadá používateľa podľa username a hesla.
     * Používa sa pri konzolom prihlasovaní.
     *
     * <p><b>POZOR:</b> Heslo sa porovnáva priamo s hodnotou v {@code password_hash} – plain text!
     *
     * @param username   hľadané používateľské meno
     * @param rawPassword heslo v nezašifrovanej forme
     * @return nájdený {@link User}, alebo null ak neexistuje zhoda
     * @throws SQLException ak nastane chyba pri prístupe k databáze
     */
    public User findByUsernameAndPassword(String username, String rawPassword) throws SQLException {
        String sql = "SELECT id, username, password_hash, role, full_name " +
                "FROM users WHERE username = ? AND password_hash = ?";
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, rawPassword);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    /**
     * Zmaže používateľa z databázy podľa jeho ID.
     * Pred zmazaním treba odstrániť všetky jeho účty (kvôli cudzím kľúčom).
     *
     * @param id ID používateľa na zmazanie
     * @throws SQLException ak nastane chyba pri prístupe k databáze
     */
    public void delete(int id) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM users WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Vráti zoznam všetkých používateľov v databáze.
     *
     * @return zoznam všetkých {@link User} objektov
     * @throws SQLException ak nastane chyba pri prístupe k databáze
     */
    public List<User> findAll() throws SQLException {
        String sql = "SELECT id, username, password_hash, role, full_name FROM users";
        try (Connection c = Db.get();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {

            List<User> out = new ArrayList<>();
            while (rs.next()) {
                out.add(mapRow(rs));
            }
            return out;
        }
    }

    /**
     * Pridá nového používateľa do databázy (bez vrátenia ID).
     * Vhodné ak ID nového záznamu nepotrebujeme.
     *
     * <p><b>Poznámka:</b> V praxi použi BCrypt hash namiesto plain textu!
     *
     * @param username    používateľské meno
     * @param rawPassword heslo v plain texte (ulož ako hash v produkcii)
     * @param role        rola – "USER" alebo "ADMIN"
     * @param fullName    celé meno používateľa
     * @throws SQLException ak nastane chyba pri prístupe k databáze
     */
    public void create(String username, String rawPassword, String role, String fullName) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role, full_name) VALUES (?, ?, ?, ?)";
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, rawPassword); // v praxi použi BCrypt hash!
            ps.setString(3, role);
            ps.setString(4, fullName);
            ps.executeUpdate();
        }
    }

    /**
     * Pridá nového používateľa a vráti jeho automaticky vygenerované ID.
     * Používa sa keď po vytvorení používateľa chceme hneď vytvoriť aj jeho účet.
     *
     * <p><b>Poznámka:</b> V praxi použi BCrypt hash namiesto plain textu!
     *
     * @param username    používateľské meno
     * @param rawPassword heslo v plain texte
     * @param role        rola – "USER" alebo "ADMIN"
     * @param fullName    celé meno používateľa
     * @return ID nového používateľa z databázy
     * @throws SQLException ak nastane chyba alebo sa nepodarí získať ID
     */
    public int createAndReturnId(String username, String rawPassword, String role, String fullName) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role, full_name) VALUES (?, ?, ?, ?)";
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, username);
            ps.setString(2, rawPassword); // v praxi použi BCrypt hash!
            ps.setString(3, role);
            ps.setString(4, fullName);
            ps.executeUpdate();

            // Získanie automaticky vygenerovaného ID nového záznamu
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                } else {
                    throw new SQLException("Nepodarilo sa získať ID nového používateľa.");
                }
            }
        }
    }

    /**
     * Pomocná metóda – namapuje jeden riadok ResultSet na objekt {@link User}.
     *
     * @param rs ResultSet nastavený na aktuálny riadok
     * @return nový objekt User s hodnotami z DB
     * @throws SQLException ak nastane chyba pri čítaní z ResultSet
     */
    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("role"),
                rs.getString("full_name")
        );
    }
}
