package recipemanager;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Die Logik der App (ohne Oberfläche): Matching, fehlende Zutaten, Umrechnung, Filter. */
public class RezeptService {

    /**
     * Vorrats-Basics: Diese Zutaten gelten immer als vorhanden. Sie müssen nicht ausgewählt werden
     * und zählen beim Matching nicht mit (sonst würde z. B. "Salz" jedes Rezept schlechter bewerten).
     */
    public static final Set<String> BASICS = Set.of(
            "salz", "pfeffer", "olivenöl", "zimt", "oregano", "paprikapulver",
            "kreuzkümmel", "currypulver", "chiliflocken", "backpulver", "vanilleextrakt",
            "muskat", "kurkuma", "garam masala", "thymian", "rosmarin", "balsamico", "sesamöl");

    /** Ergebnis des Matchings: Rezept + wie viele seiner (Nicht-Basic-)Zutaten vorhanden sind. */
    public record Treffer(Rezept rezept, int vorhanden, int gesamt) {
        public double anteil() {
            return gesamt == 0 ? 1.0 : (double) vorhanden / gesamt;
        }

        public int prozent() {
            return (int) Math.round(anteil() * 100);
        }

        public int fehlend() {
            return gesamt - vorhanden;
        }
    }

    /** Vereinheitlicht Zutatennamen für den Vergleich ("Tomaten " und "tomaten" sind gleich). */
    public static String normalisiere(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    public static Set<String> normalisiereAlle(Set<String> namen) {
        Set<String> ergebnis = new HashSet<>();
        for (String n : namen) ergebnis.add(normalisiere(n));
        return ergebnis;
    }

    public static boolean istBasic(String zutatName) {
        return BASICS.contains(normalisiere(zutatName));
    }

    /** Bewertet ein Rezept: wie viele seiner Zutaten (ohne Basics) hat der Nutzer? Parameter: normalisierte Namen. */
    public static Treffer bewerte(Rezept r, Set<String> vorhandenNormalisiert) {
        int vorhanden = 0;
        int gesamt = 0;
        for (Zutat z : r.getZutaten()) {
            if (istBasic(z.name())) continue;
            gesamt++;
            if (vorhandenNormalisiert.contains(normalisiere(z.name()))) vorhanden++;
        }
        return new Treffer(r, vorhanden, gesamt);
    }

    /**
     * Kern-Algorithmus: bewertet alle Rezepte und sortiert absteigend nach Übereinstimmung
     * (Anteil vorhandener Zutaten). Bei Gleichstand kommt das Rezept mit weniger fehlenden Zutaten
     * zuerst, danach alphabetisch.
     */
    public static List<Treffer> bewerteUndSortiere(List<Rezept> rezepte, Set<String> vorhandenNormalisiert) {
        List<Treffer> ergebnis = new ArrayList<>();
        for (Rezept r : rezepte) ergebnis.add(bewerte(r, vorhandenNormalisiert));
        ergebnis.sort(Comparator.comparingDouble(Treffer::anteil).reversed()
                .thenComparingInt(Treffer::fehlend)
                .thenComparing(t -> t.rezept().getTitel(), String.CASE_INSENSITIVE_ORDER));
        return ergebnis;
    }

    /** Wie bewerteUndSortiere, aber nur Rezepte, von denen mindestens eine Zutat vorhanden ist. */
    public static List<Treffer> findePassende(List<Rezept> rezepte, Set<String> vorhandenNormalisiert) {
        List<Treffer> ergebnis = bewerteUndSortiere(rezepte, vorhandenNormalisiert);
        ergebnis.removeIf(t -> t.vorhanden() == 0);
        return ergebnis;
    }

    /** Zutaten eines Rezepts, die der Nutzer noch nicht hat (Basics gelten als vorhanden). */
    public static List<Zutat> fehlendeZutaten(Rezept r, Set<String> vorhandenNormalisiert) {
        return r.getZutaten().stream()
                .filter(z -> !istBasic(z.name()))
                .filter(z -> !vorhandenNormalisiert.contains(normalisiere(z.name())))
                .toList();
    }

    /** Rechnet eine Menge von der Basis-Portionszahl auf die gewünschte Portionszahl um. */
    public static double skaliere(double menge, int basisPortionen, int neuePortionen) {
        return menge / basisPortionen * neuePortionen;
    }

    /** Filter nach Suchtext (Titel, Kategorie, Zutat), Schwierigkeitsgrad und maximaler Dauer. null/"Alle" = egal. */
    public static List<Rezept> filtere(List<Rezept> alle, String suchtext, String schwierigkeit, Integer maxMinuten) {
        String suche = normalisiere(suchtext);
        return alle.stream()
                .filter(r -> suche.isEmpty()
                        || normalisiere(r.getTitel()).contains(suche)
                        || normalisiere(r.getKategorie()).contains(suche)
                        || r.getZutaten().stream().anyMatch(z -> normalisiere(z.name()).contains(suche)))
                .filter(r -> schwierigkeit == null || schwierigkeit.equals("Alle")
                        || r.getSchwierigkeitsgrad().equalsIgnoreCase(schwierigkeit))
                .filter(r -> maxMinuten == null || r.getZubereitungszeit() <= maxMinuten)
                .toList();
    }

    /** Menge als lesbarer Text: 250 -> "250", 0.5 -> "0,5", 1.3333 -> "1,33". */
    public static String formatMenge(double menge) {
        return new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.GERMAN)).format(menge);
    }
}
