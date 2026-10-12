package recipemanager;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Formular zum Anlegen (r == null) oder Bearbeiten eines Rezepts. */
class FormularSeite extends Seite {

    private static final String[] KATEGORIEN = {"Frühstück", "Snack", "Shake & Dessert", "Mittagessen", "Abendessen", "Salat"};

    private final AppPanel app;
    private final Rezept rezept;   // null = neues Rezept

    private final JTextField titel = new JTextField();
    private final Ui.Chip[] kategorieChips = new Ui.Chip[KATEGORIEN.length];
    private final Ui.Chip[] schwierigkeitChips = new Ui.Chip[Rezept.SCHWIERIGKEITEN.length];
    private final JSpinner zeit = new JSpinner(new SpinnerNumberModel(30, 1, 600, 5));
    private final JSpinner portionen = new JSpinner(new SpinnerNumberModel(2, 1, 30, 1));
    private final JTextArea anleitung = new JTextArea();
    private final JPanel zutatenPanel = Ui.leer(null);
    private final List<ZutatZeile> zeilen = new ArrayList<>();
    private final JLabel fehler = new JLabel(" ");
    private final JLabel fotoInfo = new Ui.MutedLabel(" ");
    private String gewaehltesBild;   // Name, falls der Nutzer ein Foto gewählt hat

    FormularSeite(AppPanel app, Rezept rezept) {
        this.app = app;
        this.rezept = rezept;

        JPanel inhalt = new Ui.ScrollPanel(null);
        inhalt.setLayout(new BoxLayout(inhalt, BoxLayout.Y_AXIS));
        inhalt.add(new Kopfleiste(app, true, null, false));
        inhalt.add(Ui.links(Ui.umbruch(rezept == null ? "Neues Rezept" : "Rezept bearbeiten", 480, 30f, Font.BOLD)));
        inhalt.add(Box.createVerticalStrut(18));

        // Titel
        inhalt.add(beschriftung("Titel"));
        gestalte(titel, 44);
        titel.putClientProperty("JTextField.placeholderText", "z. B. Gemüse-Lasagne");
        inhalt.add(titel);
        inhalt.add(Box.createVerticalStrut(16));

        // Kategorie
        inhalt.add(beschriftung("Kategorie"));
        JPanel kat = Ui.leer(new Ui.Fluss(Ui.SPALTE - 20, 8, 8));
        kat.setAlignmentX(LEFT_ALIGNMENT);
        ButtonGroup kg = new ButtonGroup();
        for (int i = 0; i < KATEGORIEN.length; i++) {
            kategorieChips[i] = new Ui.Chip(KATEGORIEN[i]);
            kg.add(kategorieChips[i]);
            kat.add(kategorieChips[i]);
        }
        inhalt.add(kat);
        inhalt.add(Box.createVerticalStrut(16));

        // Schwierigkeit
        inhalt.add(beschriftung("Schwierigkeit"));
        JPanel sw = Ui.leer(new Ui.Fluss(Ui.SPALTE - 20, 8, 8));
        sw.setAlignmentX(LEFT_ALIGNMENT);
        ButtonGroup sg = new ButtonGroup();
        for (int i = 0; i < Rezept.SCHWIERIGKEITEN.length; i++) {
            schwierigkeitChips[i] = new Ui.Chip(Rezept.SCHWIERIGKEITEN[i]);
            sg.add(schwierigkeitChips[i]);
            sw.add(schwierigkeitChips[i]);
        }
        inhalt.add(sw);
        inhalt.add(Box.createVerticalStrut(16));

        // Zeit und Portionen
        zeit.setPreferredSize(new Dimension(100, 42));
        portionen.setPreferredSize(new Dimension(100, 42));
        JPanel zp = Ui.leer(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        zp.setAlignmentX(LEFT_ALIGNMENT);
        zp.add(new Ui.MutedLabel("Dauer (Min.)"));
        zp.add(Box.createHorizontalStrut(10));
        zp.add(zeit);
        zp.add(Box.createHorizontalStrut(30));
        zp.add(new Ui.MutedLabel("Portionen"));
        zp.add(Box.createHorizontalStrut(10));
        zp.add(portionen);
        zp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        inhalt.add(zp);
        inhalt.add(Box.createVerticalStrut(20));

        // Zutaten
        inhalt.add(beschriftung("Zutaten (Name, Menge, Einheit)"));
        zutatenPanel.setLayout(new BoxLayout(zutatenPanel, BoxLayout.Y_AXIS));
        zutatenPanel.setAlignmentX(LEFT_ALIGNMENT);
        inhalt.add(zutatenPanel);
        inhalt.add(Box.createVerticalStrut(6));
        inhalt.add(reihe(0, new Ui.PillButton("+ Zutat hinzufügen", Ui.PillButton.Stil.SEKUNDAER, () -> {
            neueZeile("", "", "");
            zutatenPanel.revalidate();
            zutatenPanel.repaint();
        })));
        inhalt.add(Box.createVerticalStrut(20));

        // Anleitung
        inhalt.add(beschriftung("Zubereitung"));
        inhalt.add(Ui.links(new Ui.MutedLabel("Jeder Satz wird in der Rezeptansicht zu einem eigenen Schritt.")));
        inhalt.add(Box.createVerticalStrut(8));
        anleitung.setLineWrap(true);
        anleitung.setWrapStyleWord(true);
        anleitung.setRows(8);
        anleitung.setMargin(new java.awt.Insets(8, 10, 8, 10));
        javax.swing.JScrollPane sp = new javax.swing.JScrollPane(anleitung);
        sp.setPreferredSize(new Dimension(Ui.SPALTE, 190));
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        sp.setAlignmentX(LEFT_ALIGNMENT);
        inhalt.add(sp);
        inhalt.add(Box.createVerticalStrut(20));

        // Foto
        inhalt.add(beschriftung("Foto (optional)"));
        inhalt.add(reihe(12,
                new Ui.PillButton("Foto auswählen …", Ui.PillButton.Stil.SEKUNDAER, this::waehleFoto), fotoInfo));
        inhalt.add(Box.createVerticalStrut(24));

        // Fehler + Knöpfe
        fehler.setForeground(Ui.ROT);
        fehler.setAlignmentX(LEFT_ALIGNMENT);
        inhalt.add(fehler);
        inhalt.add(Box.createVerticalStrut(8));
        inhalt.add(reihe(10,
                new Ui.PillButton("Speichern", Ui.PillButton.Stil.PRIMAER, this::speichern),
                new Ui.PillButton("Abbrechen", Ui.PillButton.Stil.TEXT, app::zurueck)));
        inhalt.add(Box.createVerticalStrut(40));

        fuellen();
        setzeSpalte(Ui.scroll(inhalt));
    }

    // ---------------------------------------------------------------- Aufbau

    private JLabel beschriftung(String text) {
        JLabel l = Ui.label(text, 15f, Font.BOLD);
        l.setBorder(Ui.abstand(0, 2, 8, 0));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private static void gestalte(JTextField f, int hoehe) {
        f.setPreferredSize(new Dimension(Ui.SPALTE, hoehe));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, hoehe));
        f.setAlignmentX(LEFT_ALIGNMENT);
    }

    /** Füllt das Formular mit den Daten des Rezepts (bzw. mit Standardwerten für ein neues). */
    private void fuellen() {
        if (rezept == null) {
            kategorieChips[0].setSelected(true);
            schwierigkeitChips[1].setSelected(true);
            for (int i = 0; i < 4; i++) neueZeile("", "", "");
            fotoInfo.setText("Noch kein Foto");
            return;
        }
        titel.setText(rezept.getTitel());
        boolean gefunden = false;
        for (int i = 0; i < KATEGORIEN.length; i++) {
            if (RezeptService.normalisiere(KATEGORIEN[i]).equals(RezeptService.normalisiere(rezept.getKategorie()))) {
                kategorieChips[i].setSelected(true);
                gefunden = true;
            }
        }
        if (!gefunden) kategorieChips[0].setSelected(true);
        boolean sg = false;
        for (int i = 0; i < Rezept.SCHWIERIGKEITEN.length; i++) {
            if (Rezept.SCHWIERIGKEITEN[i].equals(rezept.getSchwierigkeitsgrad())) {
                schwierigkeitChips[i].setSelected(true);
                sg = true;
            }
        }
        if (!sg) schwierigkeitChips[1].setSelected(true);
        zeit.setValue(Math.max(1, Math.min(600, rezept.getZubereitungszeit())));
        portionen.setValue(Math.max(1, Math.min(30, rezept.getPortionen())));
        anleitung.setText(rezept.getAnleitung());
        anleitung.setCaretPosition(0);
        for (Zutat z : rezept.getZutaten()) {
            neueZeile(z.name(), z.menge() == 0 ? "" : RezeptService.formatMenge(z.menge()), z.einheit());
        }
        if (zeilen.isEmpty()) neueZeile("", "", "");
        fotoInfo.setText(Bilder.vorschau(rezept.getBild()) != null ? "Foto vorhanden" : "Noch kein Foto");
    }

    private void neueZeile(String name, String menge, String einheit) {
        ZutatZeile z = new ZutatZeile(name, menge, einheit);
        zeilen.add(z);
        zutatenPanel.add(z);
        zutatenPanel.add(Box.createVerticalStrut(8));
        z.entfernen.addActionListener(e -> {
            int i = zutatenPanel.getComponentZOrder(z);
            zutatenPanel.remove(z);
            if (i < zutatenPanel.getComponentCount()) zutatenPanel.remove(i);   // den Abstand dahinter
            zeilen.remove(z);
            zutatenPanel.revalidate();
            zutatenPanel.repaint();
        });
    }

    /** Eine Eingabezeile für eine Zutat. */
    private static class ZutatZeile extends JPanel {
        final JTextField name = new JTextField();
        final JTextField menge = new JTextField();
        final JTextField einheit = new JTextField();
        final Ui.IconButton entfernen = new Ui.IconButton(Ui.Sym.MINUS, true, () -> { });

        ZutatZeile(String n, String m, String e) {
            super(null);
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            name.setText(n);
            menge.setText(m);
            einheit.setText(e);
            name.putClientProperty("JTextField.placeholderText", "Zutat");
            menge.putClientProperty("JTextField.placeholderText", "Menge");
            einheit.putClientProperty("JTextField.placeholderText", "g, ml, EL …");
            name.setPreferredSize(new Dimension(200, 40));
            name.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            menge.setPreferredSize(new Dimension(74, 40));
            menge.setMaximumSize(new Dimension(74, 40));
            einheit.setPreferredSize(new Dimension(96, 40));
            einheit.setMaximumSize(new Dimension(96, 40));
            add(name);
            add(Box.createHorizontalStrut(8));
            add(menge);
            add(Box.createHorizontalStrut(8));
            add(einheit);
            add(Box.createHorizontalStrut(8));
            add(entfernen);
            setAlignmentX(LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        }
    }

    // ---------------------------------------------------------------- Aktionen

    private void waehleFoto() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Foto auswählen");
        fc.setFileFilter(new FileNameExtensionFilter("Bilder (JPG, PNG)", "jpg", "jpeg", "png"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        String name = rezept != null ? rezept.getBild() : Bilder.slug(titel.getText());
        try {
            gewaehltesBild = Bilder.uebernehme(fc.getSelectedFile().toPath(), name);
            fotoInfo.setText("Foto gewählt: " + fc.getSelectedFile().getName());
        } catch (IOException ex) {
            fehler.setText("Das Foto konnte nicht kopiert werden: " + ex.getMessage());
        }
    }

    private void speichern() {
        String t = titel.getText().trim();
        if (t.isEmpty()) {
            fehler.setText("Bitte gib einen Titel ein.");
            return;
        }
        List<Zutat> zutaten = new ArrayList<>();
        for (ZutatZeile z : zeilen) {
            String n = z.name.getText().trim();
            String m = z.menge.getText().trim();
            String e = z.einheit.getText().trim();
            if (n.isEmpty() && m.isEmpty() && e.isEmpty()) continue;   // leere Zeile ignorieren
            if (n.isEmpty()) {
                fehler.setText("Eine Zutat hat eine Menge, aber keinen Namen.");
                return;
            }
            double menge = 0;
            if (!m.isEmpty()) {
                try {
                    menge = Double.parseDouble(m.replace(',', '.'));
                } catch (NumberFormatException ex) {
                    fehler.setText("Die Menge bei „" + n + "“ ist keine Zahl.");
                    return;
                }
                if (menge < 0) {
                    fehler.setText("Die Menge bei „" + n + "“ darf nicht negativ sein.");
                    return;
                }
            }
            zutaten.add(new Zutat(n, menge, e));
        }
        if (zutaten.isEmpty()) {
            fehler.setText("Bitte gib mindestens eine Zutat ein.");
            return;
        }

        String altesBild = rezept != null ? rezept.getBild() : null;   // vor dem Ändern des Titels festhalten
        Rezept r = rezept != null ? rezept : new Rezept();
        r.setTitel(t);
        for (int i = 0; i < KATEGORIEN.length; i++) if (kategorieChips[i].isSelected()) r.setKategorie(KATEGORIEN[i]);
        for (int i = 0; i < schwierigkeitChips.length; i++) {
            if (schwierigkeitChips[i].isSelected()) r.setSchwierigkeitsgrad(Rezept.SCHWIERIGKEITEN[i]);
        }
        r.setZubereitungszeit((Integer) zeit.getValue());
        r.setPortionen((Integer) portionen.getValue());
        r.setAnleitung(anleitung.getText().trim().replaceAll("\\s*\\n\\s*", " "));
        r.setZutaten(zutaten);
        if (gewaehltesBild != null) r.setBild(gewaehltesBild);
        else if (rezept == null) r.setBild(Bilder.slug(t));
        else r.setBild(altesBild);

        if (rezept == null) app.speichereNeu(r);
        else if (app.speichereBearbeitet(r)) app.zurueck();
    }
}
