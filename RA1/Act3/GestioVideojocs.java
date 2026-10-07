import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Gestió d'un catàleg de videojocs (CRUD complet) amb persistència binària.
 *
 * Totes les dades es guarden al fitxer videojocs.dat escrivint l'ArrayList
 * sencer amb ObjectOutputStream i es recuperen amb ObjectInputStream en
 * arrencar el programa.
 */
public class GestioVideojocs {

    private static final String FITXER = "videojocs.dat";
    private static final String FITXER_TEMPORAL = "videojocs.dat.tmp";

    /**
     * Filtre de deserialització: només s'accepten les classes que realment
     * guardem (ArrayList, Videojoc i String). Qualsevol altra classe que
     * aparegui dins del fitxer es rebutja abans de crear l'objecte.
     */
    private static final ObjectInputFilter FILTRE =
            ObjectInputFilter.Config.createFilter("java.util.ArrayList;Videojoc;java.lang.*;maxdepth=5;!*");

    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        ArrayList<Videojoc> videojocs = carregarVideojocs();

        int opcio;
        do {
            mostrarMenu();
            opcio = llegirEnter("Tria una opció: ");

            switch (opcio) {
                case 1:
                    afegirVideojoc(videojocs);
                    break;
                case 2:
                    llistarVideojocs(videojocs);
                    break;
                case 3:
                    cercarPerTitol(videojocs);
                    break;
                case 4:
                    actualitzarVideojoc(videojocs);
                    break;
                case 5:
                    eliminarVideojoc(videojocs);
                    break;
                case 6:
                    desarVideojocs(videojocs);
                    System.out.println("Canvis desats. Sortint del programa...");
                    break;
                default:
                    System.out.println("Opció incorrecta. Torna-ho a provar.");
            }
        } while (opcio != 6);

        sc.close();
    }

    private static void mostrarMenu() {
        System.out.println();
        System.out.println("===== GESTIÓ DE VIDEOJOCS =====");
        System.out.println("1. Afegir videojoc");
        System.out.println("2. Llistar tots els videojocs");
        System.out.println("3. Cercar videojocs per títol");
        System.out.println("4. Actualitzar un videojoc");
        System.out.println("5. Eliminar un videojoc");
        System.out.println("6. Sortir");
    }

    // ---------- Operacions CRUD ----------

    /**
     * CREATE: demana les dades d'un nou videojoc, l'afegeix a la llista i desa el fitxer.
     */
    private static void afegirVideojoc(ArrayList<Videojoc> videojocs) {

        System.out.println("--- Nou videojoc ---");
        String titol = llegirTextObligatori("Títol: ");
        String genere = llegirTextObligatori("Gènere (acció, rol, esport...): ");
        int any = llegirEnter("Any de llançament: ");
        String plataforma = llegirTextObligatori("Plataforma (PC, PlayStation, Xbox, Switch...): ");
        double preu = llegirDouble("Preu: ");

        videojocs.add(new Videojoc(titol, genere, any, plataforma, preu));
        desarVideojocs(videojocs);
        System.out.println("Videojoc afegit correctament.");
    }

    /**
     * READ: mostra tots els videojocs numerats.
     */
    private static void llistarVideojocs(ArrayList<Videojoc> videojocs) {

        if (videojocs.isEmpty()) {
            System.out.println("No hi ha cap videojoc al catàleg.");
            return;
        }

        System.out.println("--- Llista de videojocs (" + videojocs.size() + ") ---");
        for (int i = 0; i < videojocs.size(); i++) {
            System.out.println((i + 1) + ". " + videojocs.get(i));
        }
    }

    /**
     * READ: mostra els videojocs el títol dels quals conté el text indicat
     * (sense distingir majúscules i minúscules).
     */
    private static void cercarPerTitol(ArrayList<Videojoc> videojocs) {

        String text = llegirTextObligatori("Text a cercar al títol: ").toLowerCase();

        int trobats = 0;
        for (int i = 0; i < videojocs.size(); i++) {
            Videojoc v = videojocs.get(i);
            if (v.getTitol().toLowerCase().contains(text)) {
                System.out.println((i + 1) + ". " + v);
                trobats++;
            }
        }

        if (trobats == 0) {
            System.out.println("No s'ha trobat cap videojoc que contingui \"" + text + "\".");
        } else {
            System.out.println(trobats + " videojoc(s) trobat(s).");
        }
    }

    /**
     * UPDATE: permet canviar les dades d'un videojoc existent.
     * Si l'usuari prem Intro sense escriure res, es manté el valor actual.
     */
    private static void actualitzarVideojoc(ArrayList<Videojoc> videojocs) {

        Videojoc v = seleccionarVideojoc(videojocs, "actualitzar");
        if (v == null) {
            return;
        }

        System.out.println("Deixa el camp buit i prem Intro per mantenir el valor actual.");

        String text = llegirText("Títol [" + v.getTitol() + "]: ");
        if (!text.isEmpty()) {
            v.setTitol(text);
        }

        text = llegirText("Gènere [" + v.getGenere() + "]: ");
        if (!text.isEmpty()) {
            v.setGenere(text);
        }

        Integer any = llegirEnterOpcional("Any de llançament [" + v.getAnyLlancament() + "]: ");
        if (any != null) {
            v.setAnyLlancament(any);
        }

        text = llegirText("Plataforma [" + v.getPlataforma() + "]: ");
        if (!text.isEmpty()) {
            v.setPlataforma(text);
        }

        Double preu = llegirDoubleOpcional("Preu [" + v.getPreu() + "]: ");
        if (preu != null) {
            v.setPreu(preu);
        }

        desarVideojocs(videojocs);
        System.out.println("Videojoc actualitzat: " + v);
    }

    /**
     * DELETE: esborra un videojoc de la llista després de demanar confirmació.
     */
    private static void eliminarVideojoc(ArrayList<Videojoc> videojocs) {

        Videojoc v = seleccionarVideojoc(videojocs, "eliminar");
        if (v == null) {
            return;
        }

        String confirmacio = llegirText("Segur que vols eliminar \"" + v.getTitol() + "\"? (s/n): ");
        if (confirmacio.equalsIgnoreCase("s")) {
            videojocs.remove(v);
            desarVideojocs(videojocs);
            System.out.println("Videojoc eliminat.");
        } else {
            System.out.println("Operació cancel·lada.");
        }
    }

    /**
     * Mostra la llista i demana el número del videojoc sobre el qual es vol actuar.
     *
     * @return el videojoc triat o null si la llista és buida o el número no és vàlid
     */
    private static Videojoc seleccionarVideojoc(ArrayList<Videojoc> videojocs, String accio) {

        if (videojocs.isEmpty()) {
            System.out.println("No hi ha cap videojoc per " + accio + ".");
            return null;
        }

        llistarVideojocs(videojocs);
        int numero = llegirEnter("Número del videojoc a " + accio + ": ");

        if (numero < 1 || numero > videojocs.size()) {
            System.out.println("Número no vàlid.");
            return null;
        }

        return videojocs.get(numero - 1);
    }

    // ---------- Persistència ----------

    /**
     * Llegeix la llista de videojocs del fitxer binari amb ObjectInputStream.
     * Si el fitxer no existeix es comença amb una llista buida.
     *
     * En lloc de fer un cast directe (i haver d'utilitzar @SuppressWarnings("unchecked"))
     * es comprova que l'objecte llegit sigui una llista i que cada element sigui un Videojoc.
     */
    private static ArrayList<Videojoc> carregarVideojocs() {

        ArrayList<Videojoc> videojocs = new ArrayList<>();
        File fitxer = new File(FITXER);

        if (!fitxer.exists()) {
            System.out.println("No existeix " + FITXER + ". Es comença amb un catàleg buit.");
            return videojocs;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fitxer))) {

            ois.setObjectInputFilter(FILTRE);
            Object llegit = ois.readObject();

            if (!(llegit instanceof ArrayList<?>)) {
                System.out.println("Error: el fitxer " + FITXER + " no conté una llista de videojocs.");
                return videojocs;
            }

            for (Object element : (ArrayList<?>) llegit) {
                if (element instanceof Videojoc) {
                    videojocs.add((Videojoc) element);
                } else {
                    System.out.println("Avís: s'ignora un element que no és un Videojoc.");
                }
            }

            System.out.println("S'han carregat " + videojocs.size() + " videojocs de " + FITXER + ".");

        } catch (EOFException e) {
            System.out.println("Error: el fitxer " + FITXER + " està buit o truncat.");
        } catch (InvalidClassException e) {
            System.out.println("Error: el fitxer s'ha creat amb una versió incompatible de la classe Videojoc o "
                    + "conté classes no permeses (" + e.getMessage() + ").");
        } catch (StreamCorruptedException e) {
            System.out.println("Error: el fitxer " + FITXER + " està corrupte (" + e.getMessage() + ").");
        } catch (ClassNotFoundException e) {
            System.out.println("Error: no es troba la classe " + e.getMessage() + " necessària per llegir el fitxer.");
        } catch (IOException e) {
            System.out.println("Error carregant els videojocs: " + e.getMessage());
        }

        return videojocs;
    }

    /**
     * Escriu la llista sencera al fitxer binari amb ObjectOutputStream.
     *
     * Primer s'escriu en un fitxer temporal i, si tot ha anat bé, es substitueix
     * l'original. Així, si el programa falla a mig escriure, videojocs.dat no queda
     * truncat i no es perden les dades anteriors.
     */
    private static void desarVideojocs(ArrayList<Videojoc> videojocs) {

        File temporal = new File(FITXER_TEMPORAL);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(temporal))) {
            oos.writeObject(videojocs);
            oos.flush();
        } catch (IOException e) {
            System.out.println("Error desant els videojocs: " + e.getMessage());
            temporal.delete();
            return;
        }

        try {
            Files.move(temporal.toPath(), new File(FITXER).toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("Error substituint el fitxer " + FITXER + ": " + e.getMessage());
        }
    }

    // ---------- Lectura de dades per teclat ----------

    /**
     * Llegeix una línia de teclat. Si l'entrada s'acaba (Ctrl+Z / Ctrl+D) es
     * surt del programa; les dades ja estan desades després de cada operació.
     */
    private static String llegirText(String missatge) {
        System.out.print(missatge);
        if (!sc.hasNextLine()) {
            System.out.println();
            System.out.println("Final de l'entrada. Sortint del programa...");
            System.exit(0);
        }
        return sc.nextLine().trim();
    }

    private static String llegirTextObligatori(String missatge) {
        String text = llegirText(missatge);
        while (text.isEmpty()) {
            System.out.println("Aquest camp no pot estar buit.");
            text = llegirText(missatge);
        }
        return text;
    }

    /**
     * Llegeix un enter. Es fa amb nextLine() + parseInt per evitar el problema
     * del salt de línia que deixa nextInt() al buffer.
     */
    private static int llegirEnter(String missatge) {
        Integer valor = llegirEnterOpcional(missatge);
        while (valor == null) {
            System.out.println("Has d'introduir un número enter.");
            valor = llegirEnterOpcional(missatge);
        }
        return valor;
    }

    /**
     * @return l'enter introduït o null si la línia és buida o no és un número
     */
    private static Integer llegirEnterOpcional(String missatge) {
        String text = llegirText(missatge);
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            System.out.println("\"" + text + "\" no és un número enter vàlid.");
            return null;
        }
    }

    private static double llegirDouble(String missatge) {
        Double valor = llegirDoubleOpcional(missatge);
        while (valor == null) {
            System.out.println("Has d'introduir un número (pots fer servir coma o punt decimal).");
            valor = llegirDoubleOpcional(missatge);
        }
        return valor;
    }

    /**
     * Accepta tant "59.99" com "59,99".
     *
     * @return el número introduït o null si la línia és buida o no és vàlida
     */
    private static Double llegirDoubleOpcional(String missatge) {
        String text = llegirText(missatge);
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(text.replace(',', '.'));
        } catch (NumberFormatException e) {
            System.out.println("\"" + text + "\" no és un número vàlid.");
            return null;
        }
    }
}
