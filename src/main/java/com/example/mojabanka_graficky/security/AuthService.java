package com.example.mojabanka_graficky.security;

import com.example.mojabanka_graficky.dao.UserDao;
import com.example.mojabanka_graficky.model.User;

/**
 * Služba pre autentifikáciu používateľov v grafickom (JavaFX) rozhraní.
 * Overí prihlasovacie údaje voči databáze a pri úspechu uloží
 * prihláseného používateľa do {@link Session}.
 *
 * <p><b>TODO:</b> Heslá sú momentálne porovnávané ako plain text.
 * V produkcii nahradiť {@code BCrypt.checkpw(rawPassword, u.getPasswordHash())}.
 */
// Pozn.: použi jBCrypt alebo iný hashovací algoritmus; dočasne plain-porovnanie nahradíme neskôr
public class AuthService {

    /** DAO pre prístup k databáze používateľov. */
    private final UserDao userDao = new UserDao();

    /**
     * Overí prihlasovacie údaje a pri úspešnom overení uloží používateľa do session.
     *
     * <p>Momentálne prijíma aj heslo {@code "admin"} pre akýkoľvek účet –
     * toto je dočasné pre účely testovania a musí sa odstrániť v produkcii.
     *
     * @param username    prihlasovacie meno
     * @param rawPassword heslo v plain texte zadané používateľom
     * @return {@code true} ak prihlásenie prebehlo úspešne, {@code false} inak
     */
    public boolean login(String username, String rawPassword) {
        try {
            // Vyhľadanie používateľa podľa mena v databáze
            var opt = userDao.findByUsername(username);
            if (opt.isEmpty()) return false;  // Používateľ s daným menom neexistuje
            User u = opt.get();

            // TODO: nahradiť BCrypt.checkpw(rawPassword, u.getPasswordHash())
            // Dočasne: porovnanie plain textu alebo admin master hesla
            boolean ok = rawPassword.equals(u.getPasswordHash()) || "admin".equals(rawPassword);

            if (ok) {
                // Uloženie prihláseného používateľa do globálnej session
                Session.set(u);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
