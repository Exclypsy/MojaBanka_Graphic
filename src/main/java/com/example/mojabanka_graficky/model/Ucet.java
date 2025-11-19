package com.example.mojabanka_graficky.model;
//tst
public class Ucet {
    private long id;
    private String majitel;
    private long number;
    protected double zostatok;
    private double urok;

    public Ucet(long id, String majitel, long number, double zostatok, double urok) {
        this.id = id;
        this.majitel = majitel;
        this.number = number;
        setZostatok(zostatok);
        this.urok = urok;
    }

    public long getId() { return id; }
    public String getMajitel() { return majitel; }
    public long getNumber() { return number; }
    public double getZostatok() { return zostatok; }
    public double getUrok() { return urok; }

    public void setZostatok(double zostatok) {
        if (zostatok < 0) System.out.println("Chybna bankova operacia - zaporny zostatok");
        else this.zostatok = zostatok;
    }

    public void vklad(double suma) {
        if (suma <= 0) System.out.println("Chybna bankova operacia - zaporna alebo nulova suma vkladu");
        else this.zostatok += suma;
    }

    public void vyber(double suma) {
        if (suma <= 0) System.out.println("Chybna bankova operacia - zaporna alebo nulova suma vyberu");
        else if (suma > zostatok) System.out.println("Chybna bankova operacia - suma je vacsia ako zostatok");
        else this.zostatok -= suma;
    }

    public void zapocitajUrok() {
        zostatok += (urok / 100.0) * zostatok / 12.0;
    }
}
