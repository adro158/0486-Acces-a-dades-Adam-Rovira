import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * Xifrat i desxifrat d'un fitxer de text amb inversió de línia + xifrat Cèsar.
 *
 * Xifrar:    entrada.txt -> (invertir línia, desplaçar +clau) -> xifrat.txt
 * Desxifrar: xifrat.txt  -> (desplaçar -clau, invertir línia)  -> desxifrat.txt
 *
 * S'utilitzen els decoradors BufferedReader (sobre FileReader) i
 * BufferedWriter (sobre FileWriter).
 */
public class XifratInversio {

    private static final String FITXER_ENTRADA = "entrada.txt";
    private static final String FITXER_XIFRAT = "xifrat.txt";
    private static final String FITXER_DESXIFRAT = "desxifrat.txt";

    private static final int CLAU_PER_DEFECTE = 3;

    public static void main(String[] args) {

        int clau = demanarClau();

        System.out.println("Clau utilitzada: " + clau);
        System.out.println();

        boolean xifratCorrecte = xifrarFitxer(FITXER_ENTRADA, FITXER_XIFRAT, clau);

        if (xifratCorrecte) {
            System.out.println();
            desxifrarFitxer(FITXER_XIFRAT, FITXER_DESXIFRAT, clau);
        } else {
            System.out.println("No es desxifra perquè el xifrat no s'ha pogut fer.");
        }
    }

    /**
     * Demana la clau per consola. Si l'usuari no introdueix un número enter vàlid
     * (més gran que 0), s'utilitza la clau per defecte.
     */
    private static int demanarClau() {

        Scanner sc = new Scanner(System.in);

        System.out.print("Introdueix la clau de xifrat (enter positiu, Intro = " + CLAU_PER_DEFECTE + "): ");

        if (!sc.hasNextLine()) {
            return CLAU_PER_DEFECTE;
        }

        String text = sc.nextLine().trim();

        if (text.isEmpty()) {
            return CLAU_PER_DEFECTE;
        }

        try {
            int clau = Integer.parseInt(text);

            if (clau <= 0) {
                System.out.println("La clau ha de ser més gran que 0. S'utilitza la clau " + CLAU_PER_DEFECTE + ".");
                return CLAU_PER_DEFECTE;
            }

            return clau;

        } catch (NumberFormatException e) {
            System.out.println("\"" + text + "\" no és un número vàlid. S'utilitza la clau " + CLAU_PER_DEFECTE + ".");
            return CLAU_PER_DEFECTE;
        }
    }

    /**
     * Llegeix el fitxer d'entrada línia a línia, inverteix cada línia,
     * hi aplica el xifrat Cèsar i l'escriu al fitxer de sortida.
     *
     * @return true si el procés ha acabat correctament
     */
    public static boolean xifrarFitxer(String entrada, String sortida, int clau) {

        System.out.println("Xifrant " + entrada + " -> " + sortida + " ...");

        int numLinies = 0;

        try (
            BufferedReader br = new BufferedReader(new FileReader(entrada));
            BufferedWriter bw = new BufferedWriter(new FileWriter(sortida))
        ) {
            String linia;

            while ((linia = br.readLine()) != null) {

                String invertida = invertir(linia);
                String xifrada = desplacar(invertida, clau);

                if (numLinies > 0) {
                    bw.newLine();
                }
                bw.write(xifrada);

                numLinies++;
                System.out.println("  Línia " + numLinies + " xifrada.");
            }

            System.out.println("Xifrat completat: " + numLinies + " línies escrites a " + sortida + ".");
            return true;

        } catch (FileNotFoundException e) {
            System.out.println("Error: no s'ha trobat el fitxer " + entrada + " (" + e.getMessage() + ").");
        } catch (IOException e) {
            System.out.println("Error d'entrada/sortida durant el xifrat: " + e.getMessage());
        } catch (SecurityException e) {
            System.out.println("Error: no tens permisos per accedir als fitxers.");
        }

        return false;
    }

    /**
     * Llegeix el fitxer xifrat línia a línia, hi aplica el desplaçament invers
     * de la clau, torna a invertir la línia i escriu el resultat al fitxer de sortida.
     *
     * @return true si el procés ha acabat correctament
     */
    public static boolean desxifrarFitxer(String entrada, String sortida, int clau) {

        System.out.println("Desxifrant " + entrada + " -> " + sortida + " ...");

        int numLinies = 0;

        try (
            BufferedReader br = new BufferedReader(new FileReader(entrada));
            BufferedWriter bw = new BufferedWriter(new FileWriter(sortida))
        ) {
            String linia;

            while ((linia = br.readLine()) != null) {

                String desplacada = desplacar(linia, -clau);
                String original = invertir(desplacada);

                if (numLinies > 0) {
                    bw.newLine();
                }
                bw.write(original);

                numLinies++;
                System.out.println("  Línia " + numLinies + " desxifrada: " + original);
            }

            System.out.println("Desxifrat completat: " + numLinies + " línies escrites a " + sortida + ".");
            return true;

        } catch (FileNotFoundException e) {
            System.out.println("Error: no s'ha trobat el fitxer " + entrada + " (" + e.getMessage() + ").");
        } catch (IOException e) {
            System.out.println("Error d'entrada/sortida durant el desxifrat: " + e.getMessage());
        } catch (SecurityException e) {
            System.out.println("Error: no tens permisos per accedir als fitxers.");
        }

        return false;
    }

    /**
     * Inverteix una línia. Exemple: "Hola món" -> "nóm aloH".
     */
    public static String invertir(String linia) {
        return new StringBuilder(linia).reverse().toString();
    }

    /**
     * Xifrat Cèsar: desplaça cada caràcter N posicions en Unicode.
     * Amb un desplaçament negatiu es desfà el xifrat.
     */
    public static String desplacar(String linia, int desplacament) {

        StringBuilder resultat = new StringBuilder(linia.length());

        for (int i = 0; i < linia.length(); i++) {
            char caracter = linia.charAt(i);
            resultat.append((char) (caracter + desplacament));
        }

        return resultat.toString();
    }
}
