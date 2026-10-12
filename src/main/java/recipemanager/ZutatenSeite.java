package recipemanager;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Zutatenauswahl: alle Zutaten als antippbare Chips, nach Gruppen sortiert, mit Suchfeld. */
class ZutatenSeite extends Seite {

    private final AppPanel app;
    private final Map<String, List<Ui.Chip>> chipsJeGruppe = new LinkedHashMap<>();
    private final Map<Ui.Chip, String> chipNamen = new LinkedHashMap<>();
    private final Map<String, JLabel> ueberschriften = new LinkedHashMap<>();
    private final Map<String, JPanel> chipPanels = new LinkedHashMap<>();
    private final JLabel infoLabel = Ui.label("", 15f, Font.BOLD);
    private final Ui.PillButton zeigen;
    private final JPanel inhalt = new Ui.ScrollPanel(null);

    ZutatenSeite(AppPanel app) {
        this.app = app;
        inhalt.setLayout(new BoxLayout(inhalt, BoxLayout.Y_AXIS));

        // alle Zutaten (ohne Basics) aus den Rezepten einsammeln und gruppieren
        Map<String, String> namen = new TreeMap<>();   // normalisiert -> Anzeigename (alphabetisch)
        for (Rezept r : app.rezepte()) {
            for (Zutat z : r.getZutaten()) {
                if (!RezeptService.istBasic(z.name())) namen.putIfAbsent(RezeptService.normalisiere(z.name()), z.name());
            }
        }
        Map<String, List<String>> gruppen = new LinkedHashMap<>();
        for (String g : ZutatenKatalog.gruppenNamen()) gruppen.put(g, new ArrayList<>());
        for (String anzeige : namen.values()) gruppen.get(ZutatenKatalog.gruppeVon(anzeige)).add(anzeige);

        // Kopf: Zurück, Titel, Hinweis, Suchfeld
        JPanel kopf = Ui.leer(null);
        kopf.setLayout(new BoxLayout(kopf, BoxLayout.Y_AXIS));
        kopf.add(new Kopfleiste(app, true, null));
        kopf.add(Ui.links(Ui.umbruch("Was hast du da?", 480, 30f, Font.BOLD)));
        kopf.add(Box.createVerticalStrut(4));
        kopf.add(Ui.links(new Ui.MutedLabel("Tippe an, was du da hast. Salz, Öl und Gewürze zählen automatisch.")));
        kopf.add(Box.createVerticalStrut(14));

        JTextField suche = new JTextField();
        suche.putClientProperty("JTextField.placeholderText", "Zutat suchen …");
        suche.putClientProperty("JTextField.showClearButton", Boolean.TRUE);
        suche.setPreferredSize(new Dimension(Ui.SPALTE, 44));
        suche.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        suche.setAlignmentX(LEFT_ALIGNMENT);
        suche.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filtern(suche.getText()); }
            @Override public void removeUpdate(DocumentEvent e) { filtern(suche.getText()); }
            @Override public void changedUpdate(DocumentEvent e) { filtern(suche.getText()); }
        });
        kopf.add(suche);
        kopf.add(Box.createVerticalStrut(14));

        // Gruppen mit Chips
        for (Map.Entry<String, List<String>> e : gruppen.entrySet()) {
            if (e.getValue().isEmpty()) continue;
            JLabel ueberschrift = Ui.label(e.getKey(), 17f, Font.BOLD);
            ueberschrift.setBorder(Ui.abstand(6, 2, 10, 0));
            ueberschrift.setAlignmentX(LEFT_ALIGNMENT);
            ueberschriften.put(e.getKey(), ueberschrift);
            inhalt.add(ueberschrift);

            JPanel panel = Ui.leer(new Ui.Fluss(Ui.SPALTE - 20, 8, 8));
            panel.setAlignmentX(LEFT_ALIGNMENT);
            List<Ui.Chip> chips = new ArrayList<>();
            for (String anzeige : e.getValue()) {
                Ui.Chip chip = new Ui.Chip(anzeige);
                String norm = RezeptService.normalisiere(anzeige);
                chip.setSelected(app.auswahl().contains(norm));
                chip.addActionListener(ev -> {
                    if (chip.isSelected()) app.auswahl().add(norm);
                    else app.auswahl().remove(norm);
                    leisteAktualisieren();
                });
                chips.add(chip);
                chipNamen.put(chip, anzeige);
                panel.add(chip);
            }
            chipsJeGruppe.put(e.getKey(), chips);
            chipPanels.put(e.getKey(), panel);
            inhalt.add(panel);
            inhalt.add(Box.createVerticalStrut(10));
        }
        inhalt.add(Box.createVerticalStrut(10));

        // untere Leiste
        zeigen = new Ui.PillButton("Rezepte anzeigen", Ui.PillButton.Stil.PRIMAER, app::oeffneTreffer);
        Ui.PillButton zuruecksetzen = new Ui.PillButton("Zurücksetzen", Ui.PillButton.Stil.TEXT, () -> {
            app.auswahl().clear();
            chipNamen.keySet().forEach(c -> c.setSelected(false));
            leisteAktualisieren();
        });
        Ui.Card leiste = new Ui.Card(new BorderLayout(10, 0), 28);
        leiste.setBorder(Ui.abstand(10, 18, 10, 12));
        JPanel links = Ui.leer(new BorderLayout());
        infoLabel.setBorder(Ui.abstand(0, 18, 0, 0));   // Text bündig mit dem Text im Button
        links.add(infoLabel, BorderLayout.NORTH);
        JPanel reset = Ui.leer(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        reset.add(zuruecksetzen);
        links.add(reset, BorderLayout.SOUTH);
        leiste.add(links, BorderLayout.CENTER);
        leiste.add(zeigen, BorderLayout.EAST);
        leiste.setPreferredSize(new Dimension(Ui.SPALTE, 84));

        JPanel unten = Ui.leer(new BorderLayout());
        unten.setBorder(Ui.abstand(10, 0, 14, 0));
        unten.add(leiste, BorderLayout.CENTER);

        JPanel spalte = Ui.leer(new BorderLayout());
        spalte.add(kopf, BorderLayout.NORTH);
        spalte.add(Ui.scroll(inhalt), BorderLayout.CENTER);
        spalte.add(unten, BorderLayout.SOUTH);
        setzeSpalte(spalte);
        leisteAktualisieren();
    }

    private void leisteAktualisieren() {
        int n = app.auswahl().size();
        infoLabel.setText(n == 0 ? "Noch nichts gewählt" : n + (n == 1 ? " Zutat gewählt" : " Zutaten gewählt"));
        zeigen.setEnabled(n > 0);
        zeigen.repaint();
    }

    /** Blendet Chips aus, die nicht zum Suchtext passen (leere Gruppen verschwinden ganz). */
    private void filtern(String text) {
        String suche = RezeptService.normalisiere(text);
        for (Map.Entry<String, List<Ui.Chip>> e : chipsJeGruppe.entrySet()) {
            boolean irgendeiner = false;
            for (Ui.Chip c : e.getValue()) {
                boolean passt = suche.isEmpty() || RezeptService.normalisiere(chipNamen.get(c)).contains(suche);
                c.setVisible(passt);
                irgendeiner |= passt;
            }
            ueberschriften.get(e.getKey()).setVisible(irgendeiner);
            chipPanels.get(e.getKey()).setVisible(irgendeiner);
        }
        inhalt.revalidate();
        inhalt.repaint();
    }
}
