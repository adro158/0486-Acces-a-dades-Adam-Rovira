import java.io.Serializable;

/**
 * Representa un videojoc del catàleg.
 *
 * Implementa Serializable perquè els objectes es puguin escriure i llegir
 * amb ObjectOutputStream i ObjectInputStream.
 */
public class Videojoc implements Serializable {

    /**
     * Versió de la classe. Si no es declara, Java en calcula una a partir de
     * l'estructura de la classe i qualsevol canvi (afegir un mètode, un camp...)
     * faria que el fitxer videojocs.dat ja no es pogués llegir (InvalidClassException).
     */
    private static final long serialVersionUID = 1L;

    private String titol;
    private String genere;
    private int anyLlancament;
    private String plataforma;
    private double preu;

    public Videojoc(String titol, String genere, int anyLlancament, String plataforma, double preu) {
        this.titol = titol;
        this.genere = genere;
        this.anyLlancament = anyLlancament;
        this.plataforma = plataforma;
        this.preu = preu;
    }

    public String getTitol() {
        return titol;
    }

    public void setTitol(String titol) {
        this.titol = titol;
    }

    public String getGenere() {
        return genere;
    }

    public void setGenere(String genere) {
        this.genere = genere;
    }

    public int getAnyLlancament() {
        return anyLlancament;
    }

    public void setAnyLlancament(int anyLlancament) {
        this.anyLlancament = anyLlancament;
    }

    public String getPlataforma() {
        return plataforma;
    }

    public void setPlataforma(String plataforma) {
        this.plataforma = plataforma;
    }

    public double getPreu() {
        return preu;
    }

    public void setPreu(double preu) {
        this.preu = preu;
    }

    @Override
    public String toString() {
        return String.format("%s | Gènere: %s | Any: %d | Plataforma: %s | Preu: %.2f €",
                titol, genere, anyLlancament, plataforma, preu);
    }
}
