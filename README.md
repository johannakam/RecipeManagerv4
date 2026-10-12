# Recipe Manager v4

Desktop-App (Java 17, Swing + FlatLaf) mit moderner, mobile-App-ähnlicher Oberfläche. Die Rezepte stehen in der Datei `rezepte.json` (100 Rezepte, von leicht bis schwer), Favoriten in `favoriten.json`, Verlauf in `verlauf.json`, optionale Fotos im Ordner `bilder/`.

## Starten in IntelliJ
1. ZIP entpacken und in IntelliJ **File → Open** → die Datei `pom.xml` auswählen → "Open as Project".
2. Warten, bis Maven die Bibliotheken (Gson, FlatLaf) geladen hat (Internet nötig).
3. `src/main/java/recipemanager/Main.java` öffnen und auf den grünen Pfeil neben `main` klicken → **Run**.
4. Falls IntelliJ nach einem JDK fragt: Java 17 oder neuer wählen (File → Project Structure → Project SDK).

Die App liest beim Start `rezepte.json` im Projektordner (Arbeitsverzeichnis). Fehlt die Datei, zeigt die App einen Hinweis mit dem erwarteten Pfad.

## Bedienung
- **Startseite** "Was möchtest du heute kochen?": Kacheln Frühstück, Snack, Mittag/Abendessen und "Ich habe diese Zutaten".
- **Rezeptliste:** Karten mit Dauer und Schwierigkeit; Filter nach Schwierigkeitsgrad (leicht/mittel/schwer) und Dauer (bis 15 / 30 / 60 Min.).
- **Rezeptdetails:** alle Zutaten, Portionszahl mit +/− (alle Mengen werden automatisch umgerechnet), Zubereitung in Schritten.
- **Zutatenauswahl:** Zutaten antippen (nach Gruppen sortiert, mit Suchfeld). Danach zeigt die App die passenden Rezepte nach
  Übereinstimmung sortiert (Prozent + Balken). In den Details sind vorhandene Zutaten abgehakt, fehlende markiert und aufgelistet.
  Die Auswahl bleibt erhalten, auch in den Kategorie-Listen sieht man dann, wie gut jedes Rezept passt.
- **Favoriten:** Herz im Rezept antippen. Auf der Startseite gibt es die Kachel "Meine Favoriten". Alles ist lokal gespeichert (`favoriten.json`), die App braucht keine Internetverbindung.
- **Verlauf:** Auf der Startseite stehen "Zuletzt angesehen" (die letzten 5 Rezepte) und "Zuletzt gesucht" (die letzten 5 Zutaten-Kombinationen, antippen wiederholt die Suche). "Verlauf löschen" leert beides. Gespeichert in `verlauf.json`.
- **Rezepte verwalten:** "+ Neues Rezept" auf der Startseite; in den Details "Bearbeiten" und "Löschen" (mit Rückfrage). Änderungen werden sofort in `rezepte.json` gespeichert.
- **Fotos:** Datei `bilder/<bild>.jpg` (Namen siehe `Bilderliste.txt`). Beim Bearbeiten kann man auch ein Foto auswählen, es wird in den Ordner `bilder/` kopiert.
- **Hell/Dunkel:** Knopf oben rechts (Mond/Sonne). Das Design ist bewusst schlicht gehalten: eine Akzentfarbe (Türkisgrün, `Ui.ACCENT`), weiße Karten, feine Ränder.

Salz, Pfeffer, Olivenöl und gängige Gewürze gelten immer als vorhanden und zählen beim Matching nicht mit
(Liste: `RezeptService.BASICS`).

## Kacheln und Kategorien
| Kachel | Kategorien in der JSON |
|---|---|
| Frühstück | Frühstück |
| Snack | Snack, Shake & Dessert |
| Mittag / Abendessen | Mittagessen, Abendessen, Salat |

Die Zuordnung steht in `Bereich.java`.

## Aufbau der JSON-Datei
Eine Liste von Rezepten, jedes mit seinen Zutaten:
```json
{
  "id": 1,
  "titel": "Protein-Pfannkuchen",
  "kategorie": "Frühstück",
  "schwierigkeitsgrad": "leicht",
  "zubereitungszeit": 15,
  "portionen": 2,
  "bild": "protein-pfannkuchen",
  "anleitung": "Satz eins. Satz zwei.",
  "zutaten": [ { "name": "Eier", "menge": 3, "einheit": "Stk" } ]
}
```
`bild`: Dateiname des Fotos ohne Endung (optional; fehlt er, wird er aus dem Titel gebildet). `schwierigkeitsgrad`: leicht, mittel oder schwer. `zubereitungszeit` in Minuten. Mengen gelten für die angegebene Portionszahl.
Neue Rezepte kann man direkt in der Datei ergänzen (id weglassen ist möglich). Neue Zutaten tauchen automatisch in der Auswahl auf
(unbekannte in der Gruppe "Sonstiges", Zuordnung in `ZutatenKatalog.java`). Die Anleitung wird an Satzenden in nummerierte Schritte geteilt.

## Aufbau des Codes
| Datei | Aufgabe |
|---|---|
| `Main` | Startpunkt, öffnet das Fenster |
| `AppPanel` | Hauptfläche: Seitenwechsel, Zurück-Verlauf, gewählte Zutaten, Filter, Hell/Dunkel |
| `StartSeite`, `ListeSeite`, `DetailSeite`, `ZutatenSeite`, `FormularSeite` | die Bildschirme (Formular = neu/bearbeiten) |
| `Ui`, `Kachel`, `RezeptKarte`, `Kopfleiste`, `Seite` | Design: Farben, Symbole, Karten, Chips, Buttons |
| `RezeptService` | Logik: Matching-Algorithmus, fehlende Zutaten, Umrechnung, Filter |
| `RezeptRepository` | Lesen und Schreiben der JSON-Datei (Gson) |
| `FavoritenRepository` | Favoriten in `favoriten.json` |
| `VerlaufRepository` | Verlauf (zuletzt angesehen/gesucht) in `verlauf.json` |
| `Bilder` | lädt die Fotos aus `bilder/` |
| `Bereich`, `ZutatenKatalog` | Zuordnung der Kacheln und der Zutatengruppen |
| `Rezept`, `Zutat` | Datenklassen |

## Der Algorithmus (RezeptService.bewerteUndSortiere)
Für jedes Rezept wird gezählt, wie viele seiner Zutaten (ohne Basics) der Nutzer hat. Der Anteil vorhanden/gesamt ist die
Übereinstimmung. Sortiert wird absteigend nach diesem Anteil, bei Gleichstand zuerst das Rezept mit weniger fehlenden Zutaten,
danach alphabetisch. Zutatennamen werden vereinheitlicht (Groß-/Kleinschreibung und Leerzeichen spielen keine Rolle).

## Bekannte Einschränkungen
- Desktop-Version (Windows, macOS, Linux mit Java); keine Android-/iOS-Version.
- Zutaten werden nur über den Namen verglichen ("Tomate" und "Tomaten" wären verschieden), Einheiten werden nicht umgerechnet.
- Favoriten sind an die Rezept-`id` gebunden; ändert man ids in der JSON von Hand, passen die Favoriten nicht mehr.
