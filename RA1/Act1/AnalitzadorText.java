import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Llegeix el fitxer text.txt caràcter a caràcter amb FileReader i mostra:
 *  - el nombre de caràcters (sense comptar els salts de línia),
 *  - el nombre de línies,
 *  - el nombre de paraules,
 *  - el caràcter que apareix més vegades (sense espais, tabulacions ni salts de línia).
 *
 * Tota la informació es calcula en una única lectura del fitxer.
 */
public class AnalitzadorText {

    public static void main(String[] args) {

        String nomFitxer = "text.txt";

        int numCaracters = 0;
        int numLinies = 0;
        int numParaules = 0;

        boolean dinsParaula = false;
        boolean fitxerBuit = true;

        int[] frequencia = new int[65536];

        // Caràcter anterior: serveix per detectar el salt de línia de Windows (\r\n)
        // i per saber si l'última línia acaba o no amb un salt de línia.
        char anterior = '\0';

        try (FileReader fr = new FileReader(nomFitxer)) {

            int c;

            while ((c = fr.read()) != -1) {

                char caracter = (char) c;
                fitxerBuit = false;

                boolean esSaltLinia = (caracter == '\n' || caracter == '\r');
                boolean esSeparador = esSaltLinia || caracter == ' ' || caracter == '\t';

                // 1. Caràcters: tot menys els salts de línia
                if (!esSaltLinia) {
                    numCaracters++;
                }

                // 2. Línies:
                //    - '\r' sempre indica un salt (Windows \r\n o Mac antic \r)
                //    - '\n' només si no ve just després d'un '\r' (per no comptar dues vegades el \r\n)
                if (caracter == '\r') {
                    numLinies++;
                } else if (caracter == '\n' && anterior != '\r') {
                    numLinies++;
                }

                // 3. Paraules: comença una paraula quan passem d'un separador a un caràcter normal
                if (esSeparador) {
                    dinsParaula = false;
                } else if (!dinsParaula) {
                    dinsParaula = true;
                    numParaules++;
                }

                // 4. Freqüència dels caràcters (sense espais, tabulacions ni salts de línia)
                if (!esSeparador) {
                    frequencia[caracter]++;
                }

                anterior = caracter;
            }

            // Si el fitxer no acaba amb un salt de línia, l'última línia encara no s'ha comptat
            if (!fitxerBuit && anterior != '\n' && anterior != '\r') {
                numLinies++;
            }

            // Busquem la posició de l'array amb el valor més gran
            int posMaxima = -1;
            int maxim = 0;

            for (int i = 0; i < frequencia.length; i++) {
                if (frequencia[i] > maxim) {
                    maxim = frequencia[i];
                    posMaxima = i;
                }
            }

            System.out.println("Nombre de caràcters: " + numCaracters);
            System.out.println("Nombre de línies: " + numLinies);
            System.out.println("Nombre de paraules: " + numParaules);

            if (posMaxima == -1) {
                System.out.println("Caràcter més repetit: cap (el fitxer no conté caràcters vàlids)");
            } else {
                System.out.println("Caràcter més repetit: " + (char) posMaxima
                        + " (" + maxim + " vegades)");
            }

        } catch (FileNotFoundException e) {

            System.out.println("El fitxer " + nomFitxer + " no existeix o no es pot obrir.");

        } catch (IOException e) {

            System.out.println("S'ha produït un error de lectura: " + e.getMessage());

        } catch (SecurityException e) {

            System.out.println("No tens permisos per accedir al fitxer.");
        }
    }
}
