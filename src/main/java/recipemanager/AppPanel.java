package recipemanager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import javax.swing.JOptionPane;
import java.util.Comparator;

/**
 * Hauptfläche der App: hält den Zustand (gewählte Zutaten, Filter, Verlauf) und wechselt zwischen den Seiten.
 * Jede Seite wird beim Anzeigen neu aufgebaut, dadurch stimmen Daten und Hell/Dunkel-Farben immer.
 */
public class AppPanel extends JPanel {

    /** Gemerkte Filter der Rezeptliste. */
    static class Filter {
        String schwierigkeit = "Alle";
        Integer maxMinuten = null;

        void zuruecksetzen() {
            schwierigkeit = "Alle";
            maxMinuten = null;
        }
    }

    final Filter filter = new Filter();
    private final List<Rezept> rezepte;
    private final RezeptRepository repo;
    private final FavoritenRepository favoriten;
    private final VerlaufRepository verlaufDaten;
    private final Set<String> auswahl = new LinkedHashSet<>();   // normalisierte Namen der vorhandenen Zutaten
    private final Deque<Runnable> verlauf = new ArrayDeque<>();
    private Runnable aktuell;

    public AppPanel(RezeptRepository repo, FavoritenRepository favoriten, VerlaufRepository verlaufDaten) throws IOException {
        super(new BorderLayout());
        this.repo = repo;
        this.favoriten = favoriten;
        this.verlaufDaten = verlaufDaten;
        this.rezepte = new java.util.ArrayList<>(repo.findAll());
        zeigeStart();
    }

    List<Rezept> rezepte() { return rezepte; }

    /** Normalisierte Namen der Zutaten, die der Nutzer hat (veränderbar). */
    Set<String> auswahl() { return auswahl; }

    // ---------------------------------------------------------------- Seiten anzeigen (ohne Verlauf)

    private void zeige(JComponent seite, Runnable wiederherstellen) {
        aktuell = wiederherstellen;
        removeAll();
        add(seite, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    void zeigeStart() { zeige(new StartSeite(this), this::zeigeStart); }

    void zeigeBereich(Bereich b) {
        Supplier<List<Rezept>> basis = () -> rezepte.stream().filter(b::enthaelt).toList();
        zeige(new ListeSeite(this, b.titel(), basis, false, null), () -> zeigeBereich(b));
    }

    void zeigeTreffer() {
        zeige(new ListeSeite(this, "Das kannst du kochen", () -> rezepte, true, null), this::zeigeTreffer);
    }

    void zeigeFavoriten() {
        Supplier<List<Rezept>> basis = () -> rezepte.stream().filter(this::istFavorit).toList();
        zeige(new ListeSeite(this, "Meine Favoriten", basis, false,
                "Noch keine Favoriten. Tippe in einem Rezept auf das Herz, dann erscheint es hier."),
                this::zeigeFavoriten);
    }

    void zeigeFormular(Rezept r) { zeige(new FormularSeite(this, r), () -> zeigeFormular(r)); }

    void zeigeDetail(Rezept r) { zeige(new DetailSeite(this, r), () -> zeigeDetail(r)); }

    void zeigeZutaten() { zeige(new ZutatenSeite(this), this::zeigeZutaten); }

    // ---------------------------------------------------------------- Navigation (mit Verlauf für "Zurück")

    private void gehe(Runnable ziel) {
        if (aktuell != null) verlauf.push(aktuell);
        ziel.run();
    }

    void oeffneBereich(Bereich b) {
        filter.zuruecksetzen();
        gehe(() -> zeigeBereich(b));
    }

    void oeffneTreffer() {
        try {
            verlaufDaten.gesucht(auswahl);
        } catch (IOException e) {
            // der Verlauf ist nur eine Zugabe, ein Schreibfehler darf die Suche nicht stören
        }
        filter.zuruecksetzen();
        gehe(this::zeigeTreffer);
    }

    void oeffneFavoriten() {
        filter.zuruecksetzen();
        gehe(this::zeigeFavoriten);
    }

    void oeffneDetail(Rezept r) {
        try {
            verlaufDaten.rezeptAngesehen(r.getId());
        } catch (IOException e) {
            // siehe oben: Verlauf ist optional
        }
        gehe(() -> zeigeDetail(r));
    }

    /** Übernimmt eine frühere Suche (Zutaten) und zeigt die passenden Rezepte. */
    void wiederholeSuche(List<String> zutaten) {
        auswahl.clear();
        auswahl.addAll(zutaten);
        oeffneTreffer();
    }

    /** Zuletzt angesehene Rezepte (nur noch vorhandene), neueste zuerst. */
    List<Rezept> zuletztAngesehen() {
        List<Rezept> liste = new java.util.ArrayList<>();
        for (int id : verlaufDaten.rezepte()) {
            rezepte.stream().filter(r -> r.getId() == id).findFirst().ifPresent(liste::add);
        }
        return liste;
    }

    List<List<String>> letzteSuchen() { return verlaufDaten.suchen(); }

    boolean hatVerlauf() { return !zuletztAngesehen().isEmpty() || !verlaufDaten.suchen().isEmpty(); }

    void loescheVerlauf() {
        try {
            verlaufDaten.loeschen();
        } catch (IOException e) {
            fehler("Der Verlauf konnte nicht gelöscht werden:\n" + e.getMessage());
        }
        zeigeStart();
    }

    /** Schöner Anzeigename zu einem normalisierten Zutatennamen (aus den Rezepten). */
    String anzeigeName(String normalisiert) {
        for (Rezept r : rezepte) {
            for (Zutat z : r.getZutaten()) {
                if (RezeptService.normalisiere(z.name()).equals(normalisiert)) return z.name();
            }
        }
        return normalisiert;
    }

    /** Formular zum Bearbeiten (r != null) oder für ein neues Rezept (r == null). */
    void oeffneFormular(Rezept r) { gehe(() -> zeigeFormular(r)); }

    void oeffneZutaten() { gehe(this::zeigeZutaten); }

    void zurueck() {
        if (verlauf.isEmpty()) zeigeStart();
        else verlauf.pop().run();
    }

    // ---------------------------------------------------------------- Daten ändern

    boolean istFavorit(Rezept r) { return favoriten.istFavorit(r.getId()); }

    int anzahlFavoriten() { return (int) rezepte.stream().filter(this::istFavorit).count(); }

    void setzeFavorit(Rezept r, boolean favorit) {
        try {
            favoriten.setze(r.getId(), favorit);
        } catch (IOException e) {
            fehler("Der Favorit konnte nicht gespeichert werden:\n" + e.getMessage());
        }
    }

    /** Speichert ein bearbeitetes (bestehendes) Rezept. Gibt false zurück, wenn das Schreiben scheiterte. */
    boolean speichereBearbeitet(Rezept r) {
        try {
            repo.speichere(r);
            return true;
        } catch (IOException e) {
            fehler("Das Rezept konnte nicht gespeichert werden:\n" + e.getMessage());
            return false;
        }
    }

    /** Legt ein neues Rezept an und zeigt danach dessen Details. */
    boolean speichereNeu(Rezept r) {
        try {
            repo.speichere(r);
        } catch (IOException e) {
            fehler("Das Rezept konnte nicht gespeichert werden:\n" + e.getMessage());
            return false;
        }
        rezepte.add(r);
        rezepte.sort(Comparator.comparing(Rezept::getTitel, String.CASE_INSENSITIVE_ORDER));
        if (!verlauf.isEmpty()) verlauf.pop().run();   // das Formular verlassen, zur vorherigen Seite
        gehe(() -> zeigeDetail(r));
        return true;
    }

    /** Fragt nach und löscht das Rezept (samt Favoriten-Eintrag). */
    void loesche(Rezept r) {
        int antwort = JOptionPane.showConfirmDialog(this,
                "„" + r.getTitel() + "“ wirklich löschen?\nDas lässt sich nicht rückgängig machen.",
                "Rezept löschen", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (antwort != JOptionPane.OK_OPTION) return;
        try {
            repo.loesche(r.getId());
            favoriten.setze(r.getId(), false);
            verlaufDaten.entferneRezept(r.getId());
        } catch (IOException e) {
            fehler("Das Rezept konnte nicht gelöscht werden:\n" + e.getMessage());
            return;
        }
        rezepte.removeIf(x -> x.getId() == r.getId());
        zurueck();
    }

    private void fehler(String text) {
        JOptionPane.showMessageDialog(this, text, "Fehler", JOptionPane.ERROR_MESSAGE);
    }

    /** Wechselt zwischen hellem und dunklem Design und baut die aktuelle Seite neu auf. */
    void wechsleDesign() {
        if (Ui.dunkel()) FlatLightLaf.setup();
        else FlatDarkLaf.setup();
        FlatLaf.updateUI();
        if (aktuell != null) aktuell.run();
    }
}
