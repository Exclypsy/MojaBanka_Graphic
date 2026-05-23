package com.example.mojabanka_graficky.dao;

import com.example.mojabanka_graficky.model.Ucet;
import com.example.mojabanka_graficky.model.UcetDoMinusu;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) trieda pre operácie s bankovými účtami (tabuľka {@code accounts}).
 * Poskytuje metódy na vyhľadávanie, vytváranie, úpravu a mazanie účtov.
 * Rozlišuje medzi typom STANDARD ({@link Ucet}) a OVERDRAFT ({@link UcetDoMinusu}).
 */
public class AccountDao {

    /**
     * Vráti všetky účty patriace danému používateľovi.
     * Podľa kódu typu účtu vracia buď {@link UcetDoMinusu} alebo {@link Ucet}.
     *
     * @param userId ID používateľa
     * @return zoznam účtov daného používateľa
     * @throws SQLException ak nastane chyba pri prístupe k databáze
     */
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
                    // Rozlíšenie typu účtu podľa kódu z DB
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

    /**
     * Nájde účet podľa čísla účtu.
     * Používa sa pri prevodoch – overenie existencie cieľového účtu.
     *
     * @param number číslo bankového účtu
     * @return nájdený {@link Ucet} (alebo {@link UcetDoMinusu}), alebo null ak neexistuje
     * @throws SQLException ak nastane chyba pri prístupe k databáze
     */
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

    /**
     * Nájde účet podľa jeho ID.
     * Používa sa pri úprave účtu adminom v konzole.
     *
     * @param id ID účtu v databáze
     * @return nájdený {@link Ucet} (alebo {@link UcetDoMinusu}), alebo null ak neexistuje
     * @throws SQLException ak nastane chyba pri prístupe k databáze
     */
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

    /**
     * Vygeneruje nasledujúce číslo bankového účtu.
     * Vezme maximálne existujúce číslo a pridá 1.
     * Ak tabuľka neobsahuje žiadne účty, začína od 2002000001.
     *
     * @return nové unikátne číslo účtu
     * @throws SQLException ak nastane chyba pri prístupe k databáze
     */
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

    /**
     * Vráti všetky účty zo databázy (pre admin pohľad).
     *
     * @return zoznam všetkých účtov
     * @throws SQLException ak nastane chyba pri prístupe k databáze
     */
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

    /**
     * Aktualizuje zostatok účtu v databáze.
     * Volá sa po každej operácii (vklad, výber, úrok, prevod).
     *
     * @param accountId  ID účtu, ktorého zostatok sa má aktualizovať
     * @param newBalance nový zostatok na uloženie
     * @throws SQLException ak nastane chyba pri prístupe k databáze
     */
    public void updateBalance(long accountId, double newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance=? WHERE id=?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBigDecimal(1, BigDecimal.valueOf(newBalance));
            ps.setLong(2, accountId);
            ps.executeUpdate();
        }
    }

    /**
     * Zmaže účet z databázy podľa jeho ID.
     *
     * @param id ID účtu na zmazanie
     * @throws SQLException ak nastane chyba pri prístupe k databáze
     */
    public void delete(long id) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM accounts WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Aktualizuje kompletné informácie o účte vrátane typu a parametrov prečerpania.
     * Typ sa zadáva ako reťazec {@code "STANDARD"} alebo {@code "OVERDRAFT"}.
     * Pre STANDARD typ môžu byť {@code overdraftLimit} a {@code overdraftInterest} null.
     *
     * @param id               ID účtu na aktualizáciu
     * @param ownerName        nové meno majiteľa účtu
     * @param number           nové číslo účtu
     * @param balance          nový zostatok
     * @param interest         nový úrok (% p.a.)
     * @param typeCode         typ účtu: {@code "STANDARD"} alebo {@code "OVERDRAFT"}
     * @param overdraftLimit   limit prečerpania (len pre OVERDRAFT, inak null)
     * @param overdraftInterest úrok z prečerpania v % (len pre OVERDRAFT, inak null)
     * @throws SQLException ak nastane chyba pri prístupe k databáze
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

            // Nastavenie limitu prečerpania (null pre STANDARD)
            if (overdraftLimit != null) {
                ps.setBigDecimal(6, BigDecimal.valueOf(overdraftLimit));
            } else {
                ps.setNull(6, Types.DECIMAL);
            }

            // Nastavenie úroku z prečerpania (null pre STANDARD)
            if (overdraftInterest != null) {
                ps.setBigDecimal(7, BigDecimal.valueOf(overdraftInterest));
            } else {
                ps.setNull(7, Types.DECIMAL);
            }

            ps.setLong(8, id);
            ps.executeUpdate();
        }
    }

    /**
     * Vytvorí nový bežný (STANDARD) bankový účet pre daného používateľa.
     *
     * @param userId    ID vlastníka účtu
     * @param ownerName meno majiteľa účtu (zobrazované)
     * @param number    číslo nového účtu
     * @param balance   počiatočný zostatok
     * @param interest  úrok v % p.a.
     * @throws SQLException ak nastane chyba pri prístupe k databáze
     */
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

    /**
     * Vytvorí nový OVERDRAFT bankový účet pre daného používateľa.
     * OVERDRAFT účet umožňuje ísť do záporného zostatku do výšky limitu prečerpania.
     *
     * @param userId           ID vlastníka účtu
     * @param ownerName        meno majiteľa účtu
     * @param number           číslo nového účtu
     * @param balance          počiatočný zostatok
     * @param interest         úrok v % p.a. (pre kladný zostatok)
     * @param overdraftLimit   maximálna suma, do ktorej môže zostatok klesnúť pod nulu
     * @param overdraftInterest úrok z prečerpania v % p.a. (účtovaný keď je zostatok záporný)
     * @throws SQLException ak nastane chyba pri prístupe k databáze
     */
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
