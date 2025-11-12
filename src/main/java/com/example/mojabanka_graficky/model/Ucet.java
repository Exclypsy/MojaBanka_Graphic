import java.io.BufferedWriter;
import java.io.IOException;

public class Ucet {
    private String majitel;
    private int cislo;          //vlastnosti triedy
    double zostatok;
    private double urok;

    public Ucet(String majitel, int cislo, double zostatok, double urok) {
        this.majitel = majitel;
        this.cislo = cislo;
        setZostatok(zostatok);
        this.urok = urok;
    }


    public void setZostatok(double zostatok) {
        if (zostatok < 0) System.out.println("Chybna bankova operacia - zaporny zostatok");
        else this.zostatok = zostatok;
    }

    public void vklad(double suma){
        if (suma <= 0) System.out.println("Chybna bankova operacia - zaporna alebo nulova suma vkladu");
        else this.zostatok += suma;
    }

    public void vyber(double suma){
        if (suma <= 0) System.out.println("Chybna bankova operacia - zaporna alebo nulova suma vyberu");
        else if (suma > zostatok) System.out.println("Chybna bankova operacia - suma je vacsia ako zostatok");
        else this.zostatok -= suma;
    }

    public void zapocitajUrok(){
        zostatok += (urok/100) * zostatok/12;
    }


    public double getZostatok() {
        return zostatok;
    }


    @Override
    public String toString() {
        return "Ucet{" +
                "majitel='" + majitel + '\'' +
                ", cislo=" + cislo +
                ", zostatok=" + zostatok +
                ", urok=" + urok +
                '}';
    }

    public void save(BufferedWriter bw) throws IOException {
        bw.write(toString()+ "\n");
    }

}
