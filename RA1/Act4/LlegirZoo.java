import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Part 2: llegeix el fitxer zoo.xml amb DOM i en mostra el contingut.
 *
 * Bonus:
 *  - Compta quants animals hi ha al zoo.
 *  - Mostra només els animals que mengen "Carn" (o l'aliment que es passi
 *    com a argument, per exemple: java LlegirZoo Peix).
 */
public class LlegirZoo {

    private static final String FITXER = "zoo.xml";

    public static void main(String[] args) {

        String alimentFiltre = args.length > 0 ? args[0] : "Carn";

        File fitxer = new File(FITXER);
        if (!fitxer.exists()) {
            System.out.println("No existeix " + FITXER + ". Executa primer CrearZoo.");
            return;
        }

        try {
            Document doc = carregarDocument(fitxer);
            NodeList animals = doc.getElementsByTagName("animal");

            System.out.println("===== ANIMALS DEL ZOO =====");
            for (int i = 0; i < animals.getLength(); i++) {
                mostrarAnimal((Element) animals.item(i));
            }

            // Bonus 1: comptar els animals
            System.out.println("===== RECOMPTE =====");
            System.out.println("Hi ha " + animals.getLength() + " animals al zoo.");
            System.out.println();

            // Bonus 2: animals que mengen un aliment concret
            System.out.println("===== ANIMALS QUE MENGEN \"" + alimentFiltre.toUpperCase() + "\" =====");
            int trobats = 0;
            for (int i = 0; i < animals.getLength(); i++) {
                Element animal = (Element) animals.item(i);
                if (textFill(animal, "aliment").equalsIgnoreCase(alimentFiltre)) {
                    mostrarAnimal(animal);
                    trobats++;
                }
            }
            System.out.println(trobats + " animal(s) mengen " + alimentFiltre + ".");

        } catch (SAXException e) {
            System.out.println("Error: el fitxer " + FITXER + " no és un XML vàlid (" + e.getMessage() + ").");
        } catch (IOException e) {
            System.out.println("Error llegint el fitxer " + FITXER + ": " + e.getMessage());
        } catch (ParserConfigurationException e) {
            System.out.println("Error configurant el parser XML: " + e.getMessage());
        }
    }

    /**
     * Carrega el fitxer XML a memòria com un arbre DOM.
     *
     * Es desactiven les declaracions DOCTYPE per evitar atacs XXE (entitats
     * externes que podrien llegir fitxers del sistema), ja que el nostre XML
     * no en necessita cap.
     */
    private static Document carregarDocument(File fitxer)
            throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(fitxer);
        doc.getDocumentElement().normalize();
        return doc;
    }

    /**
     * Mostra les dades d'un <animal> amb format llegible.
     */
    private static void mostrarAnimal(Element animal) {
        System.out.println("Animal #" + animal.getAttribute("id"));
        System.out.println("Nom: " + textFill(animal, "nom"));
        System.out.println("Espècie: " + textFill(animal, "especie"));
        System.out.println("Aliment: " + textFill(animal, "aliment"));
        System.out.println("Edat: " + textFill(animal, "edat"));
        System.out.println();
    }

    /**
     * Retorna el text del primer fill de l'element amb l'etiqueta indicada,
     * o una cadena buida si no existeix.
     */
    private static String textFill(Element pare, String etiqueta) {
        NodeList llista = pare.getElementsByTagName(etiqueta);
        if (llista.getLength() == 0) {
            return "";
        }
        Node node = llista.item(0);
        return node.getTextContent().trim();
    }
}
