package com.example.mojabanka_graficky.dao;

import com.example.mojabanka_graficky.model.User;

import java.sql.*;
import java.util.Optional;

public class UserDao {
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
}
