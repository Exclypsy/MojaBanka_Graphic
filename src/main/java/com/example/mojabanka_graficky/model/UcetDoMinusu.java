package com.example.mojabanka_graficky.model;

public class UcetDoMinusu extends Ucet {

    private double povolenePrecerpanie;
    private double urokDoMinusu;

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

    @Override
    public void zapocitajUrok() {
        if (zostatok < 0) {
            double u = (-zostatok * urokDoMinusu / 100.0) / 12.0;
            zostatok -= u;
            System.out.println("Urok z precerpania: " + u + " eur");
        } else {
            super.zapocitajUrok();
        }
    }

    @Override
    public void vyber(double suma) {
        if (suma <= 0 || suma <= getZostatok()) {
            super.vyber(suma);
            return;
        }
        if (suma <= zostatok + povolenePrecerpanie) {
            zostatok -= suma;
        } else {
            System.out.println("Pokus o prekrocenie povoleneho precerpania");
        }
    }

    // ===== nové gettre, ktoré chýbali =====
    public double getPovolenePrecerpanie() {
        return povolenePrecerpanie;
    }

    public double getUrokDoMinusu() {
        return urokDoMinusu;
    }

    // prípadne aj settre, ak chceš meniť tieto hodnoty z admina:
    public void setPovolenePrecerpanie(double povolenePrecerpanie) {
        this.povolenePrecerpanie = povolenePrecerpanie;
    }

    public void setUrokDoMinusu(double urokDoMinusu) {
        this.urokDoMinusu = urokDoMinusu;
    }
}
