package recipemanager;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Font;

/** Landing Page: "Was möchtest du heute kochen?" mit den Kacheln. */
class StartSeite extends Seite {

    StartSeite(AppPanel app) {
        JPanel s = new Ui.ScrollPanel(null);
        s.setLayout(new BoxLayout(s, BoxLayout.Y_AXIS));

        s.add(new Kopfleiste(app, false, null));
        s.add(Ui.links(new Ui.MutedLabel("Recipe Manager v4")));
        s.add(Box.createVerticalStrut(6));
        s.add(Ui.links(Ui.umbruch("Was möchtest du heute kochen?", 470, 32f, Font.BOLD)));
        s.add(Box.createVerticalStrut(24));

        for (Bereich b : Bereich.values()) {
            long anzahl = app.rezepte().stream().filter(b::enthaelt).count();
            s.add(new Kachel(b.titel(), anzahl + (anzahl == 1 ? " Rezept" : " Rezepte"),
                    b.symbol(), b.farbe(), false, () -> app.oeffneBereich(b)));
            s.add(Box.createVerticalStrut(14));
        }

        int fav = app.anzahlFavoriten();
        s.add(new Kachel("Meine Favoriten", fav + (fav == 1 ? " gemerktes Rezept" : " gemerkte Rezepte"),
                Ui.Sym.HERZ, Ui.FAVORIT, false, app::oeffneFavoriten));
        s.add(Box.createVerticalStrut(14));

        int n = app.auswahl().size();
        String sub = n == 0 ? "Wähle aus, was du zu Hause hast" : n + (n == 1 ? " Zutat" : " Zutaten") + " gewählt – antippen zum Ändern";
        s.add(new Kachel("Ich habe diese Zutaten", sub, Ui.Sym.LISTE, Ui.ACCENT, true, app::oeffneZutaten));
        s.add(Box.createVerticalStrut(14));

        {
            JPanel zeile = Ui.leer(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
            zeile.setAlignmentX(LEFT_ALIGNMENT);
            if (n > 0) {
                zeile.add(new Ui.PillButton("Passende Rezepte anzeigen", Ui.PillButton.Stil.PRIMAER, app::oeffneTreffer));
                zeile.add(Box.createHorizontalStrut(10));
            }
            zeile.add(new Ui.PillButton("+ Neues Rezept", Ui.PillButton.Stil.SEKUNDAER, () -> app.oeffneFormular(null)));
            zeile.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 44));
            s.add(zeile);
            s.add(Box.createVerticalStrut(30));
        }
        if (app.hatVerlauf()) verlauf(s, app);
        setzeSpalte(Ui.scroll(s));
    }

    /** Abschnitte "Zuletzt angesehen" und "Zuletzt gesucht" mit Knopf zum Löschen. */
    private void verlauf(JPanel s, AppPanel app) {
        java.util.List<Rezept> gesehen = app.zuletztAngesehen();
        if (!gesehen.isEmpty()) {
            s.add(abschnitt("Zuletzt angesehen"));
            for (Rezept r : gesehen) {
                s.add(new RezeptKarte(r, null, app.istFavorit(r), () -> app.oeffneDetail(r)));
                s.add(Box.createVerticalStrut(10));
            }
            s.add(Box.createVerticalStrut(8));
        }
        java.util.List<java.util.List<String>> suchen = app.letzteSuchen();
        if (!suchen.isEmpty()) {
            s.add(abschnitt("Zuletzt gesucht"));
            JPanel chips = Ui.leer(new Ui.Fluss(Ui.SPALTE - 20, 8, 8));
            chips.setAlignmentX(LEFT_ALIGNMENT);
            for (java.util.List<String> suche : suchen) {
                Ui.Chip c = new Ui.Chip(beschriftung(app, suche));
                c.addActionListener(e -> app.wiederholeSuche(suche));
                chips.add(c);
            }
            s.add(chips);
            s.add(Box.createVerticalStrut(14));
        }
        JPanel zeile = Ui.leer(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        zeile.setAlignmentX(LEFT_ALIGNMENT);
        zeile.add(new Ui.PillButton("Verlauf löschen", Ui.PillButton.Stil.TEXT, app::loescheVerlauf));
        zeile.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 44));
        s.add(zeile);
        s.add(Box.createVerticalStrut(30));
    }

    private static JLabel abschnitt(String text) {
        JLabel l = Ui.label(text, 18f, Font.BOLD);
        l.setBorder(Ui.abstand(14, 2, 10, 0));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    /** "Eier, Milch, Banane +2" */
    private static String beschriftung(AppPanel app, java.util.List<String> suche) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(3, suche.size()); i++) {
            if (i > 0) sb.append(", ");
            sb.append(app.anzeigeName(suche.get(i)));
        }
        if (suche.size() > 3) sb.append(" +").append(suche.size() - 3);
        return sb.toString();
    }
}
