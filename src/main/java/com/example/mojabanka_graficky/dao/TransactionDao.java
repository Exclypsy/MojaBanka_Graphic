package com.example.mojabanka_graficky.dao;

import com.example.mojabanka_graficky.ui.admin.AdminDashboardController.TransactionView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) trieda pre operácie s transakciami (tabuľka {@code transactions}).
 * Poskytuje metódy na zaznamenávanie (logovanie) bankových operácií
 * a načítavanie histórie transakcií pre používateľov aj admina.
 */
public class TransactionDao {

    /**
     * Zaloguje jednu bankovú transakciu do databázy.
     * Používa sa po každej operácii: vklad, výber, úrok alebo prevod.
     *
     * @param userId             ID používateľa, ktorý operáciu vykonal (môže byť null – systémový úrok)
     * @param accountId          ID účtu, ktorého sa operácia týka
     * @param operationType      typ operácie: {@code DEPOSIT}, {@code WITHDRAW},
     *                           {@code TRANSFER_DEBIT}, {@code TRANSFER_CREDIT}, {@code INTEREST}
     * @param amount             suma operácie (vždy kladná)
     * @param balanceAfter       zostatok na účte po vykonaní operácie
     * @param relatedAccountId   ID druhého účtu pri prevode (null ak sa prevod netýka dvoch účtov)
     * @param description        textový popis transakcie
     * @throws Exception ak nastane chyba pri prístupe k databáze
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

            // userId môže byť null (napr. systémový záznam)
            if (userId != null) {
                ps.setInt(1, userId);
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }

            ps.setLong(2, accountId);
            ps.setString(3, operationType);
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(amount));
            ps.setBigDecimal(5, java.math.BigDecimal.valueOf(balanceAfter));

            // relatedAccountId je null pre operácie ktoré sa netýkajú dvoch účtov
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
     * Načíta všetky transakcie v systéme pre pohľad admina.
     * Obsahuje informácie o používateľovi, účte, type operácie, sume a popis.
     * Zoradené od najnovšej transakcie.
     *
     * @return zoznam všetkých transakcií ako {@link TransactionView} objekty
     * @throws Exception ak nastane chyba pri prístupe k databáze
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

                // Číselné číslo druhého účtu – null ak transakcia nemá druhý účet
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
     * Načíta transakcie pre konkrétneho používateľa – zobrazuje len jeho vlastné účty.
     * Na rozdiel od admin pohľadu neobsahuje stĺpec s username (používateľ sám seba pozná).
     * Zoradené od najnovšej transakcie.
     *
     * @param userId ID používateľa, ktorého transakcie chceme zobraziť
     * @return zoznam transakcií ako {@link TransactionUserView} objekty
     * @throws Exception ak nastane chyba pri prístupe k databáze
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

                    // Druhý účet – null ak transakcia nemá druhý účet
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
     * Read-only záznamový objekt (record) pre zobrazenie transakcie z pohľadu bežného používateľa.
     * Neobsahuje meno používateľa (na rozdiel od {@link TransactionView}).
     *
     * @param id                   ID transakcie
     * @param createdAt            dátum a čas vytvorenia transakcie
     * @param accountNumber        číslo účtu, ktorého sa transakcia týka
     * @param operationType        typ operácie (DEPOSIT, WITHDRAW, TRANSFER_DEBIT, atď.)
     * @param amount               suma operácie
     * @param balanceAfter         zostatok po operácii
     * @param relatedAccountNumber číslo druhého účtu pri prevode (alebo null)
     * @param description          popis transakcie
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
