package com.example.mojabanka_graficky.model;

/**
 * Trieda reprezentujúca prihláseného používateľa systému MojaBanka.
 * Uchováva základné identifikačné a autorizačné informácie o používateľovi.
 * Inštancie tejto triedy sa ukladajú do {@link com.example.mojabanka_graficky.security.Session}.
 */
public class User {

    /** Unikátne ID používateľa v databáze. */
    private int id;

    /** Prihlasovacie meno (login) používateľa – musí byť unikátne. */
    private String username;

    /**
     * Hashovaná hodnota hesla.
     * <b>Poznámka:</b> V aktuálnej implementácii je uložené ako plain text –
     * v produkcii je treba použiť BCrypt alebo podobné hashovanie.
     */
    private String passwordHash;

    /** Rola používateľa v systéme – buď {@code "USER"} alebo {@code "ADMIN"}. */
    private String role;

    /** Celé meno používateľa (napr. "Ján Novák") – používa sa aj ako meno majiteľa účtu. */
    private String fullName;

    /**
     * Vytvorí nový objekt používateľa so všetkými hodnotami.
     *
     * @param id           ID z databázy
     * @param username     prihlasovacie meno
     * @param passwordHash hash (alebo plain text) hesla
     * @param role         rola: "USER" alebo "ADMIN"
     * @param fullName     celé meno používateľa
     */
    public User(int id, String username, String passwordHash, String role, String fullName) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.fullName = fullName;
    }

    // ===== Gettre =====

    /** @return ID používateľa z databázy */
    public int getId() { return id; }

    /** @return prihlasovacie meno používateľa */
    public String getUsername() { return username; }

    /** @return hashované (alebo plain) heslo používateľa */
    public String getPasswordHash() { return passwordHash; }

    /** @return rola používateľa ("USER" alebo "ADMIN") */
    public String getRole() { return role; }

    /** @return celé meno používateľa */
    public String getFullName() { return fullName; }
}
