package com.example.mojabanka_graficky.dao;

import com.example.mojabanka_graficky.model.Ucet;
import com.example.mojabanka_graficky.model.UcetDoMinusu;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDao {

    public List<Ucet> findByUserId(int userId) throws SQLException {
        String sql = """
            SELECT a.id,a.owner_name,a.number,a.balance,a.interest, t.code,
                   a.overdraft_limit,a.overdraft_interest
            FROM accounts a
            JOIN account_types t ON t.id=a.type_id
            WHERE a.user_id=?
        """;
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Ucet> out = new ArrayList<>();
                while (rs.next()) {
                    String code = rs.getString("code");
                    if ("OVERDRAFT".equals(code)) {
                        out.add(new UcetDoMinusu(
                                rs.getLong("id"),
                                rs.getString("owner_name"),
                                rs.getLong("number"),
                                rs.getDouble("balance"),
                                rs.getDouble("interest"),
                                rs.getDouble("overdraft_limit"),
                                rs.getDouble("overdraft_interest")
                        ));
                    } else {
                        out.add(new Ucet(
                                rs.getLong("id"),
                                rs.getString("owner_name"),
                                rs.getLong("number"),
                                rs.getDouble("balance"),
                                rs.getDouble("interest")
                        ));
                    }
                }
                return out;
            }
        }
    }

    public List<Ucet> findAll() throws SQLException {
        String sql = """
            SELECT a.id,a.owner_name,a.number,a.balance,a.interest, t.code,
                   a.overdraft_limit,a.overdraft_interest
            FROM accounts a
            JOIN account_types t ON t.id=a.type_id
        """;
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Ucet> out = new ArrayList<>();
            while (rs.next()) {
                String code = rs.getString("code");
                if ("OVERDRAFT".equals(code)) {
                    out.add(new UcetDoMinusu(
                            rs.getLong("id"),
                            rs.getString("owner_name"),
                            rs.getLong("number"),
                            rs.getDouble("balance"),
                            rs.getDouble("interest"),
                            rs.getDouble("overdraft_limit"),
                            rs.getDouble("overdraft_interest")
                    ));
                } else {
                    out.add(new Ucet(
                            rs.getLong("id"),
                            rs.getString("owner_name"),
                            rs.getLong("number"),
                            rs.getDouble("balance"),
                            rs.getDouble("interest")
                    ));
                }
            }
            return out;
        }
    }

    public void updateBalance(long accountId, double newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance=? WHERE id=?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBigDecimal(1, BigDecimal.valueOf(newBalance));
            ps.setLong(2, accountId);
            ps.executeUpdate();
        }
    }

    // Jednoduché CRUD pre admina (skrátene)
    public void delete(long id) throws SQLException {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM accounts WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
}
