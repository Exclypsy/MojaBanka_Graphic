package com.example.mojabanka_graficky.model;

/**
 * Trieda reprezentujúca OVERDRAFT bankový účet – rozširuje {@link Ucet}.
 * Na rozdiel od štandardného účtu umožňuje ísť do záporného zostatku
 * až do výšky povoleného prečerpania ({@code povolenePrecerpanie}).
 *
 * <p>Keď je zostatok záporný, namiesto bežného úroku sa účtuje špeciálny
 * úrok z prečerpania ({@code urokDoMinusu}).
 */
public class UcetDoMinusu extends Ucet {

    /** Maximálna suma, o ktorú môže zostatok klesnúť pod nulu (napr. 500.0 = limit -500€). */
    private double povolenePrecerpanie;

    /** Ročný úrok v percentách účtovaný zo záporného zostatku (napr. 15.0 = 15% p.a.). */
    private double urokDoMinusu;

    /**
     * Vytvorí nový OVERDRAFT účet.
     *
     * @param id                  ID účtu z databázy
     * @param majitel             meno majiteľa
     * @param number              číslo účtu
     * @param zostatok            počiatočný zostatok
     * @param urok                ročný úrok pre kladný zostatok (% p.a.)
     * @param povolenePrecerpanie maximálne povolené prečerpanie (kladná hodnota)
     * @param urokDoMinusu        ročný úrok pri zápornom zostatku (% p.a.)
     */
    public UcetDoMinusu(long id,
                        String majitel,
                        long number,
                        double zostatok,
                        double urok,
                        double povolenePrecerpanie,
                        double urokDoMinusu) {
        super(id, majitel, number, zostatok, urok);
        this.povolenePrecerpanie = povolenePrecerpanie;
        this.urokDoMinusu = urokDoMinusu;
    }

    /**
     * Prepočíta a pripíše úrok pre tento OVERDRAFT účet.
     * <ul>
     *   <li>Ak je zostatok záporný: účtuje sa úrok z prečerpania (zostatok sa zhorší).</li>
     *   <li>Ak je zostatok kladný: správa sa rovnako ako štandardný účet.</li>
     * </ul>
     * Výpočet úroku z prečerpania: {@code u = (-zostatok * urokDoMinusu / 100) / 12}
     */
    @Override
    public void zapocitajUrok() {
        if (zostatok < 0) {
            // Výpočet mesačného úroku z prečerpania (zo záporného zostatku)
            double u = (-zostatok * urokDoMinusu / 100.0) / 12.0;
            zostatok -= u;  // Úrok z prečerpania znižuje zostatok (zhlboka do mínusu)
            System.out.println("Urok z precerpania: " + u + " eur");
        } else {
            // Kladný zostatok – použi štandardný výpočet úroku z rodičovskej triedy
            super.zapocitajUrok();
        }
    }

    /**
     * Odpíše sumu z OVERDRAFT účtu.
     * Ak je suma menšia alebo rovná zostatku, správa sa rovnako ako štandardný účet.
     * Ak suma prekročí zostatok, povolí výber až do výšky {@code zostatok + povolenePrecerpanie}.
     * Ak suma presahuje aj limit prečerpania, výber sa zamietne.
     *
     * @param suma suma na odpísanie (musí byť > 0)
     */
    @Override
    public void vyber(double suma) {
        if (suma <= 0 || suma <= getZostatok()) {
            // Suma je v povolenom rozsahu – deleguj na rodičovskú metódu
            super.vyber(suma);
            return;
        }
        // Suma je väčšia ako zostatok, ale skúsime prečerpanie
        if (suma <= zostatok + povolenePrecerpanie) {
            zostatok -= suma;  // Povolíme prečerpanie (zostatok bude záporný)
        } else {
            System.out.println("Pokus o prekrocenie povoleneho precerpania");
        }
    }

    // ===== Gettre pre OVERDRAFT parametre =====

    /**
     * @return maximálna povolená suma prečerpania (kladné číslo)
     */
    public double getPovolenePrecerpanie() {
        return povolenePrecerpanie;
    }

    /**
     * @return ročný úrok z prečerpania v percentách
     */
    public double getUrokDoMinusu() {
        return urokDoMinusu;
    }

    // ===== Settre pre prípadnú zmenu parametrov z admina =====

    /**
     * Nastaví nový limit povoleného prečerpania.
     *
     * @param povolenePrecerpanie nový limit (kladná hodnota)
     */
    public void setPovolenePrecerpanie(double povolenePrecerpanie) {
        this.povolenePrecerpanie = povolenePrecerpanie;
    }

    /**
     * Nastaví nový úrok z prečerpania.
     *
     * @param urokDoMinusu nový úrok v % p.a.
     */
    public void setUrokDoMinusu(double urokDoMinusu) {
        this.urokDoMinusu = urokDoMinusu;
    }
}
