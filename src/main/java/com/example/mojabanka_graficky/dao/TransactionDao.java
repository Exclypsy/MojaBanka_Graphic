package com.example.mojabanka_graficky.dao;

import com.example.mojabanka_graficky.ui.admin.AdminDashboardController.TransactionView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TransactionDao {

    /**
     * Log jednej transakcie (vklad, výber, úrok, prevod).
     *
     * @param userId             ID používateľa, ktorý akciu spravil (môže byť null – napr. systémový úrok)
     * @param accountId          ID účtu, ktorého sa operácia týka
     * @param operationType      DEPOSIT / WITHDRAW / TRANSFER_DEBIT / TRANSFER_CREDIT / INTEREST
     * @param amount             suma operácie (kladná)
     * @param balanceAfter       zostatok na účte po operácii
     * @param relatedAccountId   ID druhého účtu pri prevode (môže byť null)
     * @param description        textový popis
     */
    public void logTransaction(
            Integer userId,
            long accountId,
            String operationType,
            double amount,
            double balanceAfter,
            Long relatedAccountId,
            String description
    ) throws Exception {

        String sql = """
            INSERT INTO transactions
              (user_id, account_id, operation_type,
               amount, balance_after, related_account_id, description)
            VALUES (?,?,?,?,?,?,?)
        """;

        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            if (userId != null) {
                ps.setInt(1, userId);
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }

            ps.setLong(2, accountId);
            ps.setString(3, operationType);
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(amount));
            ps.setBigDecimal(5, java.math.BigDecimal.valueOf(balanceAfter));

            if (relatedAccountId != null) {
                ps.setLong(6, relatedAccountId);
            } else {
                ps.setNull(6, java.sql.Types.BIGINT);
            }

            ps.setString(7, description);
            ps.executeUpdate();
        }
    }

    /**
     * Všetky transakcie pre admina (vidí všetko).
     */
    public List<TransactionView> findAllForAdmin() throws Exception {
        String sql = """
            SELECT tr.id,
                   tr.created_at,
                   u.username,
                   a.number              AS account_number,
                   tr.operation_type,
                   tr.amount,
                   tr.balance_after,
                   ra.number             AS related_account_number,
                   tr.description
            FROM transactions tr
            JOIN accounts a ON a.id = tr.account_id
            LEFT JOIN accounts ra ON ra.id = tr.related_account_id
            LEFT JOIN users u ON u.id = tr.user_id
            ORDER BY tr.created_at DESC, tr.id DESC
        """;

        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<TransactionView> out = new ArrayList<>();
            while (rs.next()) {
                long id = rs.getLong("id");
                java.time.LocalDateTime createdAt =
                        rs.getTimestamp("created_at").toLocalDateTime();
                String username = rs.getString("username");
                String accNumber = String.valueOf(rs.getLong("account_number"));
                String opType = rs.getString("operation_type");
                double amount = rs.getDouble("amount");
                double balAfter = rs.getDouble("balance_after");

                String relatedAcc = null;
                long relNum = rs.getLong("related_account_number");
                if (!rs.wasNull()) {
                    relatedAcc = String.valueOf(relNum);
                }

                String desc = rs.getString("description");

                out.add(new TransactionView(
                        id, createdAt, username, accNumber,
                        opType, amount, balAfter, relatedAcc, desc
                ));
            }
            return out;
        }
    }

    /**
     * Transakcie pre konkrétneho usera – podľa jeho účtov.
     */
    public List<TransactionUserView> findForUser(int userId) throws Exception {
        String sql = """
            SELECT tr.id,
                   tr.created_at,
                   a.number              AS account_number,
                   tr.operation_type,
                   tr.amount,
                   tr.balance_after,
                   ra.number             AS related_account_number,
                   tr.description
            FROM transactions tr
            JOIN accounts a ON a.id = tr.account_id
            LEFT JOIN accounts ra ON ra.id = tr.related_account_id
            WHERE a.user_id = ?
            ORDER BY tr.created_at DESC, tr.id DESC
        """;

        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<TransactionUserView> out = new ArrayList<>();
                while (rs.next()) {
                    long id = rs.getLong("id");
                    java.time.LocalDateTime createdAt =
                            rs.getTimestamp("created_at").toLocalDateTime();
                    String accNumber = String.valueOf(rs.getLong("account_number"));
                    String opType = rs.getString("operation_type");
                    double amount = rs.getDouble("amount");
                    double balAfter = rs.getDouble("balance_after");

                    String relatedAcc = null;
                    long relNum = rs.getLong("related_account_number");
                    if (!rs.wasNull()) {
                        relatedAcc = String.valueOf(relNum);
                    }

                    String desc = rs.getString("description");

                    out.add(new TransactionUserView(
                            id, createdAt, accNumber, opType,
                            amount, balAfter, relatedAcc, desc
                    ));
                }
                return out;
            }
        }
    }

    /**
     * View objekt pre usera (bez username).
     */
    public record TransactionUserView(
            long id,
            java.time.LocalDateTime createdAt,
            String accountNumber,
            String operationType,
            double amount,
            double balanceAfter,
            String relatedAccountNumber,
            String description
    ) {}
}
