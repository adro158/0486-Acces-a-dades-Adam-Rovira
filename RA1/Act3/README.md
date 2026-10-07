# RA1 - Activitat 3: Gestió de Videojocs

Aplicació de consola en Java que gestiona un catàleg de videojocs (CRUD complet) i
guarda les dades de forma persistent al fitxer binari `videojocs.dat` utilitzant
`ObjectOutputStream` i `ObjectInputStream`.

## Fitxers

| Fitxer | Descripció |
|---|---|
| `Videojoc.java` | Classe model. Implementa `Serializable` i té els atributs `titol`, `genere`, `anyLlancament`, `plataforma` i `preu`, amb constructor, getters, setters i `toString()`. |
| `GestioVideojocs.java` | Programa principal: menú, operacions CRUD i persistència. |

## Com executar-ho

```bash
javac -encoding UTF-8 Videojoc.java GestioVideojocs.java
java GestioVideojocs
```

El fitxer `videojocs.dat` es crea automàticament al directori des d'on s'executa el programa.

## Funcionalitats

```
===== GESTIÓ DE VIDEOJOCS =====
1. Afegir videojoc
2. Llistar tots els videojocs
3. Cercar videojocs per títol
4. Actualitzar un videojoc
5. Eliminar un videojoc
6. Sortir
```

| Opció | Operació CRUD | Què fa |
|---|---|---|
| 1 | Create | Demana les dades, afegeix el `Videojoc` a l'`ArrayList` i desa el fitxer. |
| 2 | Read | Mostra tots els videojocs numerats. |
| 3 | Read | Cerca per coincidència parcial al títol, sense distingir majúscules/minúscules. |
| 4 | Update | Es tria un videojoc pel número i es canvien els camps (Intro = mantenir el valor actual). |
| 5 | Delete | Es tria un videojoc pel número i s'elimina després de confirmar-ho. |
| 6 | - | Desa tots els canvis i surt. |

Altres detalls:

- Les dades es carreguen automàticament en arrencar i es desen després de cada canvi i en sortir.
- L'entrada es llegeix sempre amb `nextLine()` + `parseInt`/`parseDouble`, així s'evita el
  problema del salt de línia que deixa `nextInt()` al buffer i el programa no peta si l'usuari
  escriu lletres on s'espera un número.
- El preu accepta tant `59.99` com `59,99`.

## Persistència

Es guarda **l'`ArrayList<Videojoc>` sencer com un sol objecte**:

```java
// Desar
try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(temporal))) {
    oos.writeObject(videojocs);
}
Files.move(temporal.toPath(), new File("videojocs.dat").toPath(), StandardCopyOption.REPLACE_EXISTING);

// Carregar
try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("videojocs.dat"))) {
    ois.setObjectInputFilter(FILTRE);
    Object llegit = ois.readObject();
    ...
}
```

Escriure la llista sencera (en comptes d'objectes solts) evita haver de detectar el final
del fitxer amb `EOFException` i el problema d'afegir objectes amb un segon `ObjectOutputStream`
(veure més avall).

---

## Problemàtiques de la serialització i dels fitxers binaris

La serialització de Java (`ObjectOutputStream`) és còmoda perquè permet guardar un objecte
amb una sola línia, però té una sèrie de problemes que cal conèixer.

### 1. Versionat de la classe (`serialVersionUID`)

Cada classe serialitzable té un identificador de versió. Si no el declarem, Java el calcula
automàticament a partir de l'estructura de la classe (nom, camps, mètodes, interfícies...).
Això vol dir que **qualsevol canvi a la classe** (afegir un mètode, un camp, canviar un tipus)
genera un identificador diferent, i en llegir un fitxer antic es llança:

```
java.io.InvalidClassException: Videojoc; local class incompatible:
stream classdesc serialVersionUID = 123..., local class serialVersionUID = 456...
```

**Conseqüència:** es perden (o deixen de ser accessibles) totes les dades guardades.

**Solució aplicada:** declarar `private static final long serialVersionUID = 1L;` a `Videojoc`.
Així es poden fer canvis compatibles (per exemple afegir un camp nou, que es llegirà amb el
valor per defecte `null`/`0`). Tot i així, canvis incompatibles (canviar el tipus d'un camp,
canviar el nom o el paquet de la classe) continuen trencant la lectura.

### 2. Dependència del nom i paquet de la classe

El fitxer guarda el nom complet de la classe (`Videojoc`, o `com.exemple.Videojoc` si està en un
paquet). Si es reanomena la classe o es mou de paquet, en llegir es produeix
`ClassNotFoundException`. També cal que **la classe existeixi al programa que llegeix** el fitxer:
un altre programa no el pot obrir si no té exactament la mateixa classe.

### 3. No és llegible ni editable per humans

El contingut és binari (comença amb els bytes màgics `AC ED 00 05`). No es pot obrir amb
el Bloc de notes per revisar o corregir una dada, ni comparar versions amb `git diff`. Si hi ha
un error a les dades, només es pot arreglar des d'un programa Java.

### 4. No és interoperable

El format és exclusiu de Java. Un programa fet en Python, JavaScript, C#... no pot llegir
`videojocs.dat`. Si les dades s'han de compartir, és millor fer servir formats estàndard com
JSON, XML o CSV, o una base de dades.

### 5. Seguretat: deserialitzar dades no fiables

És el problema més greu. `readObject()` **crea objectes de qualsevol classe** que hi hagi al
fitxer i n'executa codi (mètodes `readObject`, `readResolve`, constructors de classes pare...)
**abans** que nosaltres puguem fer el cast i comprovar què hem llegit. Un atacant pot preparar
un fitxer amb una cadena d'objectes ("gadget chain") de llibreries presents al classpath que
acabi executant ordres al sistema (*Remote Code Execution*). Ha passat en casos reals
(Apache Commons Collections el 2015, servidors WebLogic, JBoss, Jenkins...) i per això OWASP la
inclou dins de les vulnerabilitats de "deserialització insegura".

També permet atacs de denegació de servei: un fitxer petit pot generar estructures enormes
(conjunts niats, arrays gegants) que esgotin la memòria o la CPU.

**Solució aplicada:** un `ObjectInputFilter` que només permet les classes que realment
guardem i limita la profunditat:

```java
ObjectInputFilter.Config.createFilter("java.util.ArrayList;Videojoc;java.lang.*;maxdepth=5;!*");
```

Qualsevol altra classe es rebutja (`InvalidClassException: filter status: REJECTED`) abans de
crear-se. A més, en lloc d'un cast directe amb `@SuppressWarnings("unchecked")`, es comprova amb
`instanceof` que el que s'ha llegit sigui una llista i que cada element sigui un `Videojoc`.

### 6. Corrupció del fitxer i pèrdua de dades

Si el programa s'interromp mentre escriu (es tanca la finestra, es penja l'ordinador, el disc
és ple...), `new FileOutputStream("videojocs.dat")` ja ha buidat el fitxer i aquest queda
truncat. En el proper arrencament es produeix `EOFException` o `StreamCorruptedException` i
**es perd tot el catàleg**, no només l'últim canvi. A diferència d'un fitxer de text, un fitxer
binari serialitzat no es pot recuperar parcialment.

**Solució aplicada:** s'escriu primer a `videojocs.dat.tmp` i només quan l'escriptura ha acabat
bé es substitueix l'original amb `Files.move(..., REPLACE_EXISTING)`. A més, en carregar es
capturen per separat `EOFException`, `StreamCorruptedException`, `InvalidClassException` i
`ClassNotFoundException` per mostrar un missatge clar en lloc d'aturar el programa.

### 7. No es pot afegir al final del fitxer (append)

Cada `ObjectOutputStream` escriu una capçalera al principi del flux. Si fem
`new ObjectOutputStream(new FileOutputStream("fitxer.dat", true))` per afegir objectes, es
torna a escriure la capçalera al mig del fitxer i en llegir s'obté:

```
java.io.StreamCorruptedException: invalid type code: AC
```

Les alternatives són reescriure tot el fitxer (el que fem aquí), o crear una subclasse que
sobreescrigui `writeStreamHeader()` quan el fitxer ja existeix.

### 8. Saber quan s'acaba el fitxer

Si es guarden objectes un darrere l'altre, no hi ha cap mètode fiable per saber si en queden
més: `available()` retorna els bytes que es poden llegir sense bloquejar, no els objectes que
queden. L'única manera és llegir fins que salti `EOFException`, utilitzant una excepció per
controlar el flux normal del programa. Guardant l'`ArrayList` sencer es llegeix un sol objecte i
el problema desapareix.

### 9. Rendiment i escalabilitat

- Per modificar un sol videojoc cal **reescriure tot el fitxer**: no hi ha accés directe a
  un registre concret com en una base de dades o un `RandomAccessFile`.
- Cal **carregar tot el fitxer a memòria**. Amb milers o milions de registres no és viable.
- El fitxer conté metadades (nom de classe, noms i tipus dels camps), així que per a pocs
  objectes no és tan compacte com sembla.
- La serialització de Java és relativament lenta comparada amb altres formats binaris.

### 10. Concurrència

Si dos programes (o dues instàncies del mateix) obren `videojocs.dat` alhora, cadascun té la
seva còpia a memòria i l'últim que desa sobreescriu els canvis de l'altre. No hi ha cap
mecanisme de bloqueig ni de transaccions com en una base de dades.

### 11. Altres detalls a tenir en compte

- **Tots els objectes referenciats han de ser serialitzables.** Si `Videojoc` tingués un camp
  d'una classe que no implementa `Serializable` es llançaria `NotSerializableException`
  (es pot evitar marcant el camp com a `transient`, però llavors no es guarda).
- Els camps `static` i `transient` **no es guarden**.
- Si s'escriu el mateix objecte dues vegades amb el mateix `ObjectOutputStream`, la segona
  vegada només es guarda una referència a la primera: els canvis fets entremig no es guarden
  si no es crida `reset()`.
- En deserialitzar **no s'executa el constructor** de la classe, de manera que les
  validacions del constructor es poden saltar amb un fitxer manipulat.

### Resum

| Problema | Com s'ha tractat en aquesta pràctica |
|---|---|
| Canvis a la classe | `serialVersionUID = 1L` declarat explícitament |
| Fitxers maliciosos | `ObjectInputFilter` amb llista blanca de classes + comprovació amb `instanceof` |
| Fitxer truncat o corrupte | Escriptura a fitxer temporal + substitució; excepcions específiques en carregar |
| Append amb capçalera duplicada / detecció d'EOF | Es guarda l'`ArrayList` sencer en un sol `writeObject` |
| No llegible / no interoperable / escalabilitat / concurrència | Limitacions inherents del format; per a dades reals és millor JSON, XML o una base de dades |
