package recipemanager;

import java.util.ArrayList;
import java.util.List;

/** Ein Rezept mit allen zugehörigen Daten. Die id ist 0, solange das Rezept noch nicht gespeichert wurde. */
public class Rezept {

    public static final String[] SCHWIERIGKEITEN = {"leicht", "mittel", "schwer"};

    private int id;
    private String titel = "";
    private String kategorie = "";
    private String schwierigkeitsgrad = SCHWIERIGKEITEN[0];
    private int zubereitungszeit = 30;   // in Minuten
    private int portionen = 2;           // Basis-Portionszahl, auf die sich die Mengen beziehen
    private String bild = "";           // Dateiname des Fotos (ohne Endung) im Ordner bilder/
    private String anleitung = "";
    private List<Zutat> zutaten = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitel() { return titel; }
    public void setTitel(String titel) { this.titel = titel; }

    public String getKategorie() { return kategorie; }
    public void setKategorie(String kategorie) { this.kategorie = kategorie; }

    public String getSchwierigkeitsgrad() { return schwierigkeitsgrad; }
    public void setSchwierigkeitsgrad(String schwierigkeitsgrad) { this.schwierigkeitsgrad = schwierigkeitsgrad; }

    public int getZubereitungszeit() { return zubereitungszeit; }
    public void setZubereitungszeit(int zubereitungszeit) { this.zubereitungszeit = zubereitungszeit; }

    public int getPortionen() { return portionen; }
    public void setPortionen(int portionen) { this.portionen = portionen; }

    /** Dateiname des Fotos ohne Endung. Fehlt er in der Datei, wird er aus dem Titel abgeleitet. */
    public String getBild() { return (bild == null || bild.isBlank()) ? Bilder.slug(titel) : bild.trim(); }
    public void setBild(String bild) { this.bild = bild; }

    public String getAnleitung() { return anleitung; }
    public void setAnleitung(String anleitung) { this.anleitung = anleitung; }

    public List<Zutat> getZutaten() { return zutaten; }
    public void setZutaten(List<Zutat> zutaten) { this.zutaten = zutaten; }

    @Override
    public String toString() {
        return titel;   // wird von JList als Anzeigetext verwendet
    }
}
