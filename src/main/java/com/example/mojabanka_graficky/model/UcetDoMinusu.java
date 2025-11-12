package com.example.mojabanka_graficky.model;

public class UcetDoMinusu extends Ucet {
    private int povolenePrecerpanie;
    private double urokDoMinusu;

    public UcetDoMinusu(String majitel, int cislo, double zostatok, double urok, int povolenePrecerpanie, double urokDoMinusu) {
        super(majitel, cislo, zostatok, urok);
        this.povolenePrecerpanie = povolenePrecerpanie;
        this.urokDoMinusu = urokDoMinusu;
    }

    public void zapocitajUrok() {
        if (zostatok < 0) {
            double u = (-zostatok * urokDoMinusu / 100) /12;
            zostatok -= u;
            System.out.println("Urok z precerpania: " + u + "eur");
        } else {
            super.zapocitajUrok();
        }
    }

    @Override
    public void vyber(double suma){
        if (suma <= 0 || suma <= getZostatok()) super.vyber(suma);
        else {
            if (suma <= zostatok+povolenePrecerpanie) {
                zostatok -= suma;
            }
            else {
                System.out.println("Pokus o prekrocenie povoleneho precerpania");
            }
        }

    }


    @Override
    public String toString() {
        return "com.example.mojabanka_graficky.model.UcetDoMinusu{" + super.toString() +
                ", povolenePrecerpanie=" + povolenePrecerpanie +
                ", urokDoMinusu=" + urokDoMinusu +
                '}';
    }
}
