import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Part 1: crea el fitxer zoo.xml amb DOM.
 *
 * Es construeix l'arbre del document a memòria (arrel <zoo> amb diversos
 * <animal>) i després es bolca al fitxer amb un Transformer.
 */
public class CrearZoo {

    private static final String FITXER = "zoo.xml";

    /** Dades dels animals: id, nom, espècie, aliment i edat. */
    private static final String[][] ANIMALS = {
            {"1", "Pingüí", "Aptenodytes forsteri", "Peix", "5"},
            {"2", "Lleó", "Panthera leo", "Carn", "8"},
            {"3", "Elefant", "Loxodonta africana", "Herba", "25"},
            {"4", "Tigre", "Panthera tigris", "Carn", "6"},
            {"5", "Girafa", "Giraffa camelopardalis", "Fulles", "10"},
            {"6", "Llop", "Canis lupus", "Carn", "4"},
            {"7", "Os panda", "Ailuropoda melanoleuca", "Bambú", "12"}
    };

    public static void main(String[] args) {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            // Element arrel <zoo>
            Element zoo = doc.createElement("zoo");
            doc.appendChild(zoo);

            for (String[] dades : ANIMALS) {
                zoo.appendChild(crearAnimal(doc, dades[0], dades[1], dades[2], dades[3], dades[4]));
            }

            desarDocument(doc, new File(FITXER));
            System.out.println("S'ha creat el fitxer " + FITXER + " amb " + ANIMALS.length + " animals.");

        } catch (ParserConfigurationException e) {
            System.out.println("Error creant el document XML: " + e.getMessage());
        } catch (TransformerException | IOException e) {
            System.out.println("Error escrivint el fitxer " + FITXER + ": " + e.getMessage());
        }
    }

    /**
     * Crea un element <animal id="..."> amb els fills <nom>, <especie>, <aliment> i <edat>.
     */
    private static Element crearAnimal(Document doc, String id, String nom, String especie,
                                       String aliment, String edat) {

        Element animal = doc.createElement("animal");
        animal.setAttribute("id", id);

        animal.appendChild(crearElementText(doc, "nom", nom));
        animal.appendChild(crearElementText(doc, "especie", especie));
        animal.appendChild(crearElementText(doc, "aliment", aliment));
        animal.appendChild(crearElementText(doc, "edat", edat));

        return animal;
    }

    /**
     * Crea un element del tipus <etiqueta>text</etiqueta>.
     */
    private static Element crearElementText(Document doc, String etiqueta, String text) {
        Element element = doc.createElement(etiqueta);
        element.appendChild(doc.createTextNode(text));
        return element;
    }

    /**
     * Escriu el document al fitxer en UTF-8 i amb sagnat de 4 espais.
     *
     * La declaració <?xml ...?> l'escrivim nosaltres perquè el Transformer del
     * JDK la posa a la mateixa línia que l'element arrel.
     */
    private static void desarDocument(Document doc, File fitxer) throws TransformerException, IOException {

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();

        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        try (Writer writer = Files.newBufferedWriter(fitxer.toPath(), StandardCharsets.UTF_8)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.write(System.lineSeparator());
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
        }
    }
}
