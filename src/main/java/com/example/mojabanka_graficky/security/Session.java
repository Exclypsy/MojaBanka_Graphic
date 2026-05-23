package com.example.mojabanka_graficky.security;

import com.example.mojabanka_graficky.model.User;

/**
 * Správca session (prihlásenia) pre aplikáciu MojaBanka.
 * Uchováva aktuálne prihláseného používateľa v statickom atribúte,
 * ktorý je dostupný z celej aplikácie (singleton vzor).
 *
 * <p>Typické použitie:
 * <pre>
 *   Session.set(user);         // po úspešnom prihlásení
 *   Session.get().getId();     // kdekoľvek v aplikácii
 *   Session.isAdmin();         // kontrola roly
 *   Session.clear();           // po odhlásení
 * </pre>
 */
public class Session {

    /** Aktuálne prihlásený používateľ. Null ak nie je nikto prihlásený. */
    private static User current;

    /**
     * Nastaví (uloží) prihláseného používateľa do session.
     * Volá sa po úspešnom overení prihlasovacích údajov.
     *
     * @param u prihlásený používateľ
     */
    public static void set(User u) { current = u; }

    /**
     * Vráti aktuálne prihláseného používateľa.
     *
     * @return prihlásený {@link User}, alebo null ak nie je nikto prihlásený
     */
    public static User get() { return current; }

    /**
     * Skontroluje, či je prihlásený používateľ admin.
     *
     * @return true ak je prihlásený používateľ s rolou "ADMIN", inak false
     */
    public static boolean isAdmin() { return current != null && "ADMIN".equals(current.getRole()); }

    /**
     * Vyčistí session – odhlási aktuálneho používateľa.
     * Volá sa po kliknutí na Odhlásiť alebo pri návrate na prihlasovaciu obrazovku.
     */
    public static void clear() { current = null; }
}
