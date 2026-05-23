package com.example.mojabanka_graficky.model;

/**
 * Trieda reprezentujúca štandardný bankový účet.
 * Uchováva základné informácie o účte: ID, majiteľ, číslo účtu, zostatok a úrok.
 * Poskytuje metódy na vklad, výber a výpočet mesačného úroku.
 *
 * <p>Slúži ako nadtrieda pre {@link UcetDoMinusu} (OVERDRAFT účet).
 */
public class Ucet {

    /** Unikátne ID účtu v databáze. */
    private long id;

    /** Meno majiteľa účtu (zobrazované v UI). */
    private String majitel;

    /** Číslo bankového účtu (dlhé číselné ID). */
    private long number;

    /** Aktuálny zostatok na účte. Chránený prístup pre podtriedy. */
    protected double zostatok;

    /** Ročný úrok v percentách (napr. 2.5 = 2,5% p.a.). */
    private double urok;

    /**
     * Vytvorí nový účet so zadanými hodnotami.
     *
     * @param id       ID účtu z databázy
     * @param majitel  meno majiteľa
     * @param number   číslo účtu
     * @param zostatok počiatočný zostatok
     * @param urok     úrok v % p.a.
     */
    public Ucet(long id, String majitel, long number, double zostatok, double urok) {
        this.id = id;
        this.majitel = majitel;
        this.number = number;
        setZostatok(zostatok);
        this.urok = urok;
    }

    // ===== Gettre =====

    /** @return ID účtu v databáze */
    public long getId() { return id; }

    /** @return meno majiteľa účtu */
    public String getMajitel() { return majitel; }

    /** @return číslo bankového účtu */
    public long getNumber() { return number; }

    /** @return aktuálny zostatok na účte */
    public double getZostatok() { return zostatok; }

    /** @return ročný úrok v percentách */
    public double getUrok() { return urok; }

    /**
     * Nastaví zostatok na účte.
     * Záporný zostatok nie je povolený pre štandardný účet – vypíše chybovú správu.
     *
     * @param zostatok nový zostatok (musí byť >= 0)
     */
    public void setZostatok(double zostatok) {
        if (zostatok < 0) System.out.println("Chybna bankova operacia - zaporny zostatok");
        else this.zostatok = zostatok;
    }

    /**
     * Pripíše sumu na účet (vklad).
     * Suma musí byť kladná, inak vypíše chybovú správu.
     *
     * @param suma suma na pripísanie (musí byť > 0)
     */
    public void vklad(double suma) {
        if (suma <= 0) System.out.println("Chybna bankova operacia - zaporna alebo nulova suma vkladu");
        else this.zostatok += suma;
    }

    /**
     * Odpíše sumu z účtu (výber).
     * Suma musí byť kladná a nesmie presahovať aktuálny zostatok.
     * (Pre OVERDRAFT účet je logika odlišná – pozri {@link UcetDoMinusu#vyber(double)})
     *
     * @param suma suma na odpísanie (musí byť > 0 a <= zostatok)
     */
    public void vyber(double suma) {
        if (suma <= 0) System.out.println("Chybna bankova operacia - zaporna alebo nulova suma vyberu");
        else if (suma > zostatok) System.out.println("Chybna bankova operacia - suma je vacsia ako zostatok");
        else this.zostatok -= suma;
    }

    /**
     * Pripíše mesačný úrok na zostatok.
     * Výpočet: {@code zostatok += (urok / 100) * zostatok / 12}
     * (ročný úrok vydelený 12 mesiacmi = mesačný úrok)
     */
    public void zapocitajUrok() {
        zostatok += (urok / 100.0) * zostatok / 12.0;
    }
}
