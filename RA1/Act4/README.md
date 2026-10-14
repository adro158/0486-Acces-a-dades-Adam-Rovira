# RA1 - Activitat 4: El zoo virtual

Dues aplicacions en Java que treballen amb un fitxer XML (`zoo.xml`) fent servir **DOM**
(`javax.xml.parsers` + `org.w3c.dom`).

## Fitxers

| Fitxer | Descripció |
|---|---|
| `CrearZoo.java` | **Part 1.** Construeix l'arbre DOM amb l'arrel `<zoo>` i diversos `<animal id="...">` (amb `<nom>`, `<especie>`, `<aliment>` i `<edat>`) i l'escriu a `zoo.xml` amb un `Transformer`. |
| `LlegirZoo.java` | **Part 2.** Llegeix `zoo.xml`, mostra tots els animals i fa els bonus: compta els animals i mostra només els que mengen `Carn`. |
| `zoo.xml` | Fitxer generat per `CrearZoo` (s'inclou com a exemple). |

## Com executar-ho

```bash
javac -encoding UTF-8 CrearZoo.java LlegirZoo.java
java CrearZoo          # crea zoo.xml
java LlegirZoo         # mostra els animals, el recompte i els que mengen Carn
java LlegirZoo Peix    # opcional: filtra per un altre aliment
```

## Exemple de sortida (`LlegirZoo`)

```
===== ANIMALS DEL ZOO =====
Animal #1
Nom: Pingüí
Espècie: Aptenodytes forsteri
Aliment: Peix
Edat: 5

Animal #2
Nom: Lleó
Espècie: Panthera leo
Aliment: Carn
Edat: 8
...
===== RECOMPTE =====
Hi ha 7 animals al zoo.

===== ANIMALS QUE MENGEN "CARN" =====
Animal #2
Nom: Lleó
...
3 animal(s) mengen Carn.
```

## Detalls

- **Creació:** `doc.createElement()`, `setAttribute("id", ...)`, `createTextNode()` i
  `appendChild()`. El `Transformer` escriu en UTF-8 amb sagnat de 4 espais. La declaració
  `<?xml ...?>` s'escriu a part perquè el `Transformer` del JDK la posa a la mateixa línia
  que `<zoo>`.
- **Lectura:** `getElementsByTagName("animal")` per obtenir tots els animals,
  `getAttribute("id")` per l'atribut i `getTextContent()` per al text de cada fill.
- **Bonus:** el recompte és `NodeList.getLength()`; el filtre compara `<aliment>` sense
  distingir majúscules/minúscules (per defecte `Carn`, o el primer argument del programa).
- **Errors:** si `zoo.xml` no existeix s'avisa que cal executar `CrearZoo`, i si l'XML està mal
  format es mostra un missatge (`SAXException`) en lloc d'aturar el programa.
- **Seguretat:** el parser té desactivat el `DOCTYPE` per evitar atacs XXE (entitats externes
  que podrien llegir fitxers del sistema).
