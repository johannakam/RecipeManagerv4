package recipemanager;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import java.util.Set;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/** Rezeptliste einer Kategorie-Kachel oder das Ergebnis der Zutatensuche, mit Filtern. */
class ListeSeite extends Seite {

    private final AppPanel app;
    private final Supplier<List<Rezept>> basis;
    private final String leerText;
    private final boolean trefferModus;
    private final JPanel liste = new Ui.ScrollPanel(null);
    private final JLabel anzahl = new Ui.MutedLabel(" ");

    ListeSeite(AppPanel app, String titel, Supplier<List<Rezept>> basis, boolean trefferModus, String leerText) {
        this.app = app;
        this.basis = basis;
        this.leerText = leerText;
        this.trefferModus = trefferModus;
        liste.setLayout(new BoxLayout(liste, BoxLayout.Y_AXIS));

        JPanel kopf = Ui.leer(null);
        kopf.setLayout(new BoxLayout(kopf, BoxLayout.Y_AXIS));
        kopf.add(new Kopfleiste(app, true, null));
        kopf.add(Ui.links(Ui.umbruch(titel, 480, 30f, Font.BOLD)));
        kopf.add(Ui.links(anzahl));
        kopf.add(Box.createVerticalStrut(14));

        AppPanel.Filter f = app.filter;
        String[] schwierigkeiten = {"Alle", "leicht", "mittel", "schwer"};
        kopf.add(chipZeile("Schwierigkeit", schwierigkeiten, indexVon(schwierigkeiten, f.schwierigkeit), i -> {
            f.schwierigkeit = schwierigkeiten[i];
            aktualisiere();
        }));
        kopf.add(Box.createVerticalStrut(8));
        String[] zeiten = {"Beliebig", "15 Min.", "30 Min.", "60 Min."};
        Integer[] grenzen = {null, 15, 30, 60};
        int zeitIndex = f.maxMinuten == null ? 0 : f.maxMinuten <= 15 ? 1 : f.maxMinuten <= 30 ? 2 : 3;
        kopf.add(chipZeile("Dauer bis", zeiten, zeitIndex, i -> {
            f.maxMinuten = grenzen[i];
            aktualisiere();
        }));
        kopf.add(Box.createVerticalStrut(14));

        if (!app.auswahl().isEmpty()) {
            kopf.add(zutatenLeiste());
            kopf.add(Box.createVerticalStrut(14));
        }

        JPanel spalte = Ui.leer(new BorderLayout());
        spalte.add(kopf, BorderLayout.NORTH);
        spalte.add(Ui.scroll(liste), BorderLayout.CENTER);
        setzeSpalte(spalte);
        aktualisiere();
    }

    private static int indexVon(String[] werte, String wert) {
        for (int i = 0; i < werte.length; i++) if (werte[i].equals(wert)) return i;
        return 0;
    }

    private JPanel chipZeile(String beschriftung, String[] namen, int start, IntConsumer beiWahl) {
        JPanel z = Ui.leer(null);
        z.setLayout(new BoxLayout(z, BoxLayout.X_AXIS));
        JLabel l = new Ui.MutedLabel(beschriftung);
        Dimension d = new Dimension(104, 36);
        l.setPreferredSize(d);
        l.setMinimumSize(d);
        l.setMaximumSize(d);
        z.add(l);
        ButtonGroup gruppe = new ButtonGroup();
        for (int i = 0; i < namen.length; i++) {
            Ui.Chip c = new Ui.Chip(namen[i]);
            c.setSelected(i == start);
            gruppe.add(c);
            final int index = i;
            c.addActionListener(e -> beiWahl.accept(index));
            z.add(c);
            z.add(Box.createHorizontalStrut(8));
        }
        z.add(Box.createHorizontalGlue());
        z.setAlignmentX(LEFT_ALIGNMENT);
        z.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return z;
    }

    private JPanel zutatenLeiste() {
        int n = app.auswahl().size();
        Ui.Card k = new Ui.Card(new BorderLayout(), 24)
                .setFarben(Ui.mit(Ui.ACCENT, 28), Ui.mit(Ui.ACCENT, 110));
        k.setBorder(Ui.abstand(6, 18, 6, 10));
        JLabel l = Ui.label(n + (n == 1 ? " Zutat gewählt" : " Zutaten gewählt"), 14f, Font.BOLD);
        k.add(l, BorderLayout.WEST);
        k.add(new Ui.PillButton("Ändern", Ui.PillButton.Stil.TEXT, app::oeffneZutaten), BorderLayout.EAST);
        k.setAlignmentX(LEFT_ALIGNMENT);
        k.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        k.setPreferredSize(new Dimension(Ui.SPALTE, 56));
        return k;
    }

    /** Baut die Kartenliste anhand der aktuellen Filter neu auf. */
    void aktualisiere() {
        AppPanel.Filter f = app.filter;
        Set<String> vorhanden = app.auswahl();
        List<Rezept> gefiltert = RezeptService.filtere(basis.get(), "", f.schwierigkeit, f.maxMinuten);

        liste.removeAll();
        int n = 0;
        if (trefferModus || !vorhanden.isEmpty()) {
            List<RezeptService.Treffer> treffer = RezeptService.bewerteUndSortiere(gefiltert, vorhanden);
            if (trefferModus) treffer.removeIf(t -> t.vorhanden() == 0);
            for (RezeptService.Treffer t : treffer) {
                liste.add(new RezeptKarte(t.rezept(), t, app.istFavorit(t.rezept()), () -> app.oeffneDetail(t.rezept())));
                liste.add(Box.createVerticalStrut(10));
                n++;
            }
        } else {
            for (Rezept r : gefiltert) {
                liste.add(new RezeptKarte(r, null, app.istFavorit(r), () -> app.oeffneDetail(r)));
                liste.add(Box.createVerticalStrut(10));
                n++;
            }
        }
        if (n == 0) {
            boolean nurLeer = leerText != null && basis.get().isEmpty();
            JComponent leer = Ui.umbruch(nurLeer ? leerText : trefferModus
                    ? "Mit diesen Zutaten und Filtern gibt es leider keinen Treffer. Probiere andere Filter oder wähle mehr Zutaten."
                    : "Keine Rezepte für diese Filter. Probiere eine andere Auswahl.", 470, 15f, Font.PLAIN);
            leer.setBorder(Ui.abstand(24, 0, 0, 0));
            leer.setAlignmentX(LEFT_ALIGNMENT);
            liste.add(leer);
        }
        anzahl.setText(n + (n == 1 ? " Rezept" : " Rezepte"));
        liste.revalidate();
        liste.repaint();
    }
}
