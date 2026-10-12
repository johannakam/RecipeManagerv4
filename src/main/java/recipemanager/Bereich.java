package recipemanager;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

/** Die drei Kacheln der Startseite und welche Rezept-Kategorien aus der JSON-Datei dazugehören. */
public enum Bereich {
    FRUEHSTUECK("Frühstück", Ui.Sym.TASSE, Ui.ACCENT, "Frühstück"),
    SNACK("Snack", Ui.Sym.APFEL, Ui.ACCENT, "Snack", "Shake & Dessert"),
    HAUPTGERICHT("Mittag / Abendessen", Ui.Sym.BESTECK, Ui.ACCENT, "Mittagessen", "Abendessen", "Salat");

    private final String titel;
    private final Ui.Sym symbol;
    private final Color farbe;
    private final Set<String> kategorien = new HashSet<>();

    Bereich(String titel, Ui.Sym symbol, Color farbe, String... kategorien) {
        this.titel = titel;
        this.symbol = symbol;
        this.farbe = farbe;
        for (String k : kategorien) this.kategorien.add(RezeptService.normalisiere(k));
    }

    public String titel() { return titel; }
    public Ui.Sym symbol() { return symbol; }
    public Color farbe() { return farbe; }

    public boolean enthaelt(Rezept r) {
        return kategorien.contains(RezeptService.normalisiere(r.getKategorie()));
    }
}
