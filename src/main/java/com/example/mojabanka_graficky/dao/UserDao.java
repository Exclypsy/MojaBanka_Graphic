package com.example.mojabanka_graficky.dao;

import com.example.mojabanka_graficky.model.User;

import java.sql.*;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    // Login podľa používateľského mena
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

    // Zoznam všetkých používateľov
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

    // Pridanie nového používateľa (bez potreby ID)
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

    // Pridanie nového používateľa a vrátenie jeho ID (na vytvorenie účtu)
    public int createAndReturnId(String username, String rawPassword, String role, String fullName) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role, full_name) VALUES (?, ?, ?, ?)";
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, username);
            ps.setString(2, rawPassword); // v praxi použi BCrypt hash!
            ps.setString(3, role);
            ps.setString(4, fullName);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                } else {
                    throw new SQLException("Nepodarilo sa získať ID nového používateľa.");
                }
            }
        }
    }

    // Pomocná metóda na mapovanie riadku ResultSet -> User
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
