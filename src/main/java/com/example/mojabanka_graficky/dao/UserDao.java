package com.example.mojabanka_graficky.dao;

import com.example.mojabanka_graficky.model.User;

import java.sql.*;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    // Login podľa používateľského mena
    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, role, full_name FROM users WHERE username=?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("role"),
                            rs.getString("full_name")
                    ));
                }
                return Optional.empty();
            }
        }
    }

    // Zoznam všetkých užívateľov
    public List<User> findAll() throws SQLException {
        String sql = "SELECT id, username, password_hash, role, full_name FROM users";
        try (Connection c = Db.get(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            List<User> out = new ArrayList<>();
            while (rs.next()) {
                out.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role"),
                        rs.getString("full_name")
                ));
            }
            return out;
        }
    }

    // Pridanie nového užívateľa
    public void create(String username, String rawPassword, String role, String fullName) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role, full_name) VALUES (?, ?, ?, ?)";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, rawPassword); // v praxi použi BCrypt hash!
            ps.setString(3, role);
            ps.setString(4, fullName);
            ps.executeUpdate();
        }
    }
}
