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

    /** Nájde účet podľa čísla účtu. */
    public Ucet findByNumber(long number) throws SQLException {
        String sql = """
            SELECT a.id,a.owner_name,a.number,a.balance,a.interest, t.code,
                   a.overdraft_limit,a.overdraft_interest
            FROM accounts a
            JOIN account_types t ON t.id=a.type_id
            WHERE a.number = ?
        """;
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, number);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String code = rs.getString("code");
                    if ("OVERDRAFT".equals(code)) {
                        return new UcetDoMinusu(
                                rs.getLong("id"),
                                rs.getString("owner_name"),
                                rs.getLong("number"),
                                rs.getDouble("balance"),
                                rs.getDouble("interest"),
                                rs.getDouble("overdraft_limit"),
                                rs.getDouble("overdraft_interest")
                        );
                    } else {
                        return new Ucet(
                                rs.getLong("id"),
                                rs.getString("owner_name"),
                                rs.getLong("number"),
                                rs.getDouble("balance"),
                                rs.getDouble("interest")
                        );
                    }
                }
            }
        }
        return null;
    }

    /** Nájde účet podľa ID (pre AdminConsoleMenu.updateAccount). */
    public Ucet findById(int id) throws SQLException {
        String sql = """
            SELECT a.id,a.owner_name,a.number,a.balance,a.interest, t.code,
                   a.overdraft_limit,a.overdraft_interest
            FROM accounts a
            JOIN account_types t ON t.id=a.type_id
            WHERE a.id = ?
        """;
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String code = rs.getString("code");
                    if ("OVERDRAFT".equals(code)) {
                        return new UcetDoMinusu(
                                rs.getLong("id"),
                                rs.getString("owner_name"),
                                rs.getLong("number"),
                                rs.getDouble("balance"),
                                rs.getDouble("interest"),
                                rs.getDouble("overdraft_limit"),
                                rs.getDouble("overdraft_interest")
                        );
                    } else {
                        return new Ucet(
                                rs.getLong("id"),
                                rs.getString("owner_name"),
                                rs.getLong("number"),
                                rs.getDouble("balance"),
                                rs.getDouble("interest")
                        );
                    }
                }
            }
        }
        return null;
    }

    public long generateNextAccountNumber() throws SQLException {
        String sql = "SELECT COALESCE(MAX(number), 2002000000) + 1 AS next_num FROM accounts";
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("next_num");
            }
            return 2002000001L;
        }
    }

    public List<Ucet> findAll() throws SQLException {
        String sql = """
            SELECT a.id,a.owner_name,a.number,a.balance,a.interest, t.code,
                   a.overdraft_limit,a.overdraft_interest
            FROM accounts a
            JOIN account_types t ON t.id=a.type_id
        """;
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql);
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

    public void delete(long id) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM accounts WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    // ========== nové metódy pre admin dashboard ==========

    /**
     * Úprava kompletného účtu (vrátane typu a prečerpania).
     * typeCode: "STANDARD" alebo "OVERDRAFT"
     * overdraftLimit a overdraftInterest môžu byť null, ak je typ STANDARD.
     */
    public void updateAccount(long id,
                              String ownerName,
                              long number,
                              double balance,
                              double interest,
                              String typeCode,
                              Double overdraftLimit,
                              Double overdraftInterest) throws SQLException {

        String sql = """
            UPDATE accounts
            SET owner_name=?,
                number=?,
                balance=?,
                interest=?,
                type_id=(SELECT id FROM account_types WHERE code=?),
                overdraft_limit=?,
                overdraft_interest=?
            WHERE id=?
        """;

        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ownerName);
            ps.setLong(2, number);
            ps.setBigDecimal(3, BigDecimal.valueOf(balance));
            ps.setBigDecimal(4, BigDecimal.valueOf(interest));
            ps.setString(5, typeCode);

            if (overdraftLimit != null) {
                ps.setBigDecimal(6, BigDecimal.valueOf(overdraftLimit));
            } else {
                ps.setNull(6, Types.DECIMAL);
            }

            if (overdraftInterest != null) {
                ps.setBigDecimal(7, BigDecimal.valueOf(overdraftInterest));
            } else {
                ps.setNull(7, Types.DECIMAL);
            }

            ps.setLong(8, id);
            ps.executeUpdate();
        }
    }

    /** Vytvorenie bežného (STANDARD) účtu pre daného používateľa. */
    public void createStandard(int userId,
                               String ownerName,
                               long number,
                               double balance,
                               double interest) throws SQLException {

        String sql = """
            INSERT INTO accounts
              (user_id, owner_name, number, balance, interest, type_id)
            VALUES
              (?, ?, ?, ?, ?, (SELECT id FROM account_types WHERE code='STANDARD'))
        """;

        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, ownerName);
            ps.setLong(3, number);
            ps.setBigDecimal(4, BigDecimal.valueOf(balance));
            ps.setBigDecimal(5, BigDecimal.valueOf(interest));
            ps.executeUpdate();
        }
    }

    /** Vytvorenie OVERDRAFT účtu pre daného používateľa. */
    public void createOverdraft(int userId,
                                String ownerName,
                                long number,
                                double balance,
                                double interest,
                                double overdraftLimit,
                                double overdraftInterest) throws SQLException {

        String sql = """
            INSERT INTO accounts
              (user_id, owner_name, number, balance, interest,
               type_id, overdraft_limit, overdraft_interest)
            VALUES
              (?, ?, ?, ?, ?, 
               (SELECT id FROM account_types WHERE code='OVERDRAFT'),
               ?, ?)
        """;

        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, ownerName);
            ps.setLong(3, number);
            ps.setBigDecimal(4, BigDecimal.valueOf(balance));
            ps.setBigDecimal(5, BigDecimal.valueOf(interest));
            ps.setBigDecimal(6, BigDecimal.valueOf(overdraftLimit));
            ps.setBigDecimal(7, BigDecimal.valueOf(overdraftInterest));
            ps.executeUpdate();
        }
    }
}
