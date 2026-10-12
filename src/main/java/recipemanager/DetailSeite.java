package recipemanager;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Rezeptdetails: Portionen, alle Zutaten (mit Status vorhanden/fehlt) und die Zubereitung. */
class DetailSeite extends Seite {

    private final AppPanel app;
    private final Rezept rezept;
    private int portionen;
    private final JLabel portionenZahl = Ui.label("", 20f, Font.BOLD);
    private final Ui.Card zutatenKarte = new Ui.Card(null, 24);

    DetailSeite(AppPanel app, Rezept rezept) {
        this.app = app;
        this.rezept = rezept;
        this.portionen = rezept.getPortionen();

        JPanel inhalt = new Ui.ScrollPanel(null);
        inhalt.setLayout(new BoxLayout(inhalt, BoxLayout.Y_AXIS));

        inhalt.add(new Kopfleiste(app, true, null));
        java.awt.image.BufferedImage foto = Bilder.gross(rezept.getBild());
        if (foto != null) {
            inhalt.add(new Banner(foto));
            inhalt.add(Box.createVerticalStrut(16));
        }
        JPanel titelZeile = Ui.leer(new BorderLayout(10, 0));
        titelZeile.add(Ui.umbruch(rezept.getTitel(), 430, 30f, Font.BOLD), BorderLayout.CENTER);
        JPanel herz = Ui.leer(new BorderLayout());
        herz.add(new Ui.HerzButton(app.istFavorit(rezept), f -> app.setzeFavorit(rezept, f)), BorderLayout.NORTH);
        titelZeile.add(herz, BorderLayout.EAST);
        titelZeile.setAlignmentX(LEFT_ALIGNMENT);
        inhalt.add(titelZeile);
        inhalt.add(Box.createVerticalStrut(12));

        String kategorie = rezept.getKategorie().isBlank() ? "Ohne Kategorie" : rezept.getKategorie();
        inhalt.add(reihe(8,
                new Ui.Tag(kategorie, Ui.ACCENT, null),
                new Ui.Tag(rezept.getZubereitungszeit() + " Min.", Ui.ACCENT, Ui.Sym.UHR),
                new Ui.Tag(rezept.getSchwierigkeitsgrad(), Ui.ACCENT, null)));
        inhalt.add(Box.createVerticalStrut(18));

        inhalt.add(portionenKarte());
        inhalt.add(Box.createVerticalStrut(14));

        Set<String> vorhanden = app.auswahl();
        if (!vorhanden.isEmpty()) {
            inhalt.add(statusKarte(vorhanden));
            inhalt.add(Box.createVerticalStrut(14));
        }

        inhalt.add(abschnitt("Zutaten"));
        zutatenKarte.setLayout(new BoxLayout(zutatenKarte, BoxLayout.Y_AXIS));
        zutatenKarte.setBorder(Ui.abstand(6, 18, 6, 18));
        zutatenKarte.setAlignmentX(LEFT_ALIGNMENT);
        fuelleZutaten();
        inhalt.add(zutatenKarte);
        inhalt.add(Box.createVerticalStrut(18));

        inhalt.add(abschnitt("Zubereitung"));
        inhalt.add(schritteKarte());
        inhalt.add(Box.createVerticalStrut(22));

        inhalt.add(reihe(10,
                new Ui.PillButton("Bearbeiten", Ui.PillButton.Stil.SEKUNDAER, () -> app.oeffneFormular(rezept)),
                new Ui.PillButton("Löschen", Ui.PillButton.Stil.TEXT, () -> app.loesche(rezept))));
        inhalt.add(Box.createVerticalStrut(30));

        setzeSpalte(Ui.scroll(inhalt));
    }

    private static Color schwierigkeitsFarbe(String s) {
        return switch (RezeptService.normalisiere(s)) {
            case "leicht" -> Ui.GRUEN;
            case "mittel" -> new Color(0xF08C00);
            default -> Ui.ROT;
        };
    }

    private JComponent abschnitt(String text) {
        JLabel l = Ui.label(text, 20f, Font.BOLD);
        l.setBorder(Ui.abstand(0, 2, 10, 0));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JComponent portionenKarte() {
        Ui.Card k = new Ui.Card(new BorderLayout(), 24);
        k.setBorder(Ui.abstand(10, 18, 10, 12));
        k.add(Ui.label("Portionen", 16f, Font.BOLD), BorderLayout.WEST);

        portionenZahl.setHorizontalAlignment(JLabel.CENTER);
        portionenZahl.setPreferredSize(new Dimension(52, 40));
        portionenZahl.setText(String.valueOf(portionen));
        JPanel stepper = Ui.leer(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 4, 0));
        stepper.add(new Ui.IconButton(Ui.Sym.MINUS, true, () -> aendere(-1)));
        stepper.add(portionenZahl);
        stepper.add(new Ui.IconButton(Ui.Sym.PLUS, true, () -> aendere(1)));
        k.add(stepper, BorderLayout.EAST);

        k.setAlignmentX(LEFT_ALIGNMENT);
        k.setPreferredSize(new Dimension(Ui.SPALTE, 64));
        k.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        return k;
    }

    private void aendere(int delta) {
        int neu = Math.max(1, Math.min(30, portionen + delta));
        if (neu == portionen) return;
        portionen = neu;
        portionenZahl.setText(String.valueOf(portionen));
        fuelleZutaten();   // alle Mengen automatisch umrechnen
    }

    private JComponent statusKarte(Set<String> vorhanden) {
        RezeptService.Treffer t = RezeptService.bewerte(rezept, vorhanden);
        List<Zutat> fehlend = RezeptService.fehlendeZutaten(rezept, vorhanden);
        boolean alles = fehlend.isEmpty();
        Color farbe = alles ? Ui.GRUEN : Ui.ACCENT;

        Ui.Card k = new Ui.Card(null, 24).setFarben(Ui.mit(farbe, 28), Ui.mit(farbe, 110));
        k.setLayout(new BoxLayout(k, BoxLayout.Y_AXIS));
        k.setBorder(Ui.abstand(16, 18, 16, 18));

        String titel = alles ? "Du hast alle Zutaten – guten Appetit!"
                : "Dir " + (fehlend.size() == 1 ? "fehlt noch 1 Zutat" : "fehlen noch " + fehlend.size() + " Zutaten");
        k.add(Ui.links(Ui.label(titel, 17f, Font.BOLD)));
        k.add(Box.createVerticalStrut(4));
        k.add(Ui.links(new Ui.MutedLabel(t.vorhanden() + " von " + t.gesamt() + " Zutaten vorhanden")));
        k.add(Box.createVerticalStrut(10));
        k.add(Ui.links(new Ui.Balken(t.anteil(), farbe)));
        if (!alles) {
            k.add(Box.createVerticalStrut(12));
            String namen = fehlend.stream().map(Zutat::name).collect(Collectors.joining(", "));
            k.add(Ui.links(Ui.umbruch("Fehlt: " + namen, 470, 14f, Font.PLAIN)));
        }
        k.setAlignmentX(LEFT_ALIGNMENT);
        return k;
    }

    private void fuelleZutaten() {
        zutatenKarte.removeAll();
        Set<String> vorhanden = app.auswahl();
        boolean aktiv = !vorhanden.isEmpty();
        List<Zutat> zutaten = rezept.getZutaten();
        for (int i = 0; i < zutaten.size(); i++) {
            Zutat z = zutaten.get(i);
            boolean da = RezeptService.istBasic(z.name()) || vorhanden.contains(RezeptService.normalisiere(z.name()));
            double menge = RezeptService.skaliere(z.menge(), rezept.getPortionen(), portionen);
            String mengeText = (menge == 0 ? "" : RezeptService.formatMenge(menge) + " ") + z.einheit();
            zutatenKarte.add(new ZutatZeile(z.name(), mengeText.trim(), aktiv, da, i < zutaten.size() - 1));
        }
        zutatenKarte.revalidate();
        zutatenKarte.repaint();
    }

    private JComponent schritteKarte() {
        Ui.Card k = new Ui.Card(null, 24);
        k.setLayout(new BoxLayout(k, BoxLayout.Y_AXIS));
        k.setBorder(Ui.abstand(16, 18, 10, 18));
        k.setAlignmentX(LEFT_ALIGNMENT);

        String text = rezept.getAnleitung().trim();
        if (text.isEmpty()) {
            k.add(Ui.links(new Ui.MutedLabel("Keine Anleitung hinterlegt.")));
            k.add(Box.createVerticalStrut(6));
            return k;
        }
        String[] teile = text.split("(?<=[.!?])\\s+");
        for (int i = 0; i < teile.length; i++) {
            JPanel zeile = Ui.leer(new BorderLayout(14, 0));
            JPanel west = Ui.leer(new BorderLayout());
            west.add(new Nummer(i + 1), BorderLayout.NORTH);
            zeile.add(west, BorderLayout.WEST);
            JComponent l = Ui.umbruch(teile[i], 430, 15f, Font.PLAIN);
            l.setBorder(Ui.abstand(4, 0, 0, 0));
            zeile.add(l, BorderLayout.CENTER);
            zeile.setAlignmentX(LEFT_ALIGNMENT);
            k.add(zeile);
            k.add(Box.createVerticalStrut(14));
        }
        return k;
    }

    /** Großes Foto oben auf der Detailseite (abgerundet, zugeschnitten). */
    private static class Banner extends JComponent {
        private final java.awt.image.BufferedImage bild;

        Banner(java.awt.image.BufferedImage bild) {
            this.bild = bild;
            setAlignmentX(LEFT_ALIGNMENT);
        }

        @Override public Dimension getPreferredSize() { return new Dimension(Ui.SPALTE, 230); }
        @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 230); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Ui.g2(g);
            int w = getWidth(), h = getHeight();
            g2.clip(new java.awt.geom.RoundRectangle2D.Double(0, 0, w, h, 28, 28));
            Ui.zeichneZugeschnitten(g2, bild, 0, 0, w, h);
            g2.dispose();
        }
    }

    /** Runde Schrittnummer. */
    private static class Nummer extends JComponent {
        private final int nummer;

        Nummer(int nummer) {
            this.nummer = nummer;
            setPreferredSize(new Dimension(30, 30));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Ui.g2(g);
            g2.setColor(Ui.mit(Ui.ACCENT, 40));
            g2.fill(new Ellipse2D.Double(0, 0, 29, 29));
            g2.setColor(Ui.ACCENT);
            g2.setFont(Ui.schrift(14f, Font.BOLD));
            Ui.textMitte(g2, String.valueOf(nummer), 15, 15);
            g2.dispose();
        }
    }

    /** Eine Zeile der Zutatenliste: Status-Symbol, Name, Menge (und "fehlt"-Etikett). */
    private static class ZutatZeile extends JComponent {
        private final String name;
        private final String menge;
        private final boolean aktiv;
        private final boolean vorhanden;
        private final boolean trennlinie;

        ZutatZeile(String name, String menge, boolean aktiv, boolean vorhanden, boolean trennlinie) {
            this.name = name;
            this.menge = menge;
            this.aktiv = aktiv;
            this.vorhanden = vorhanden;
            this.trennlinie = trennlinie;
            setAlignmentX(LEFT_ALIGNMENT);
        }

        @Override public Dimension getPreferredSize() { return new Dimension(100, 48); }
        @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 48); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Ui.g2(g);
            int w = getWidth(), h = getHeight();
            int cy = h / 2;

            // Status-Symbol
            if (!aktiv) {
                g2.setColor(Ui.ACCENT);
                g2.fill(new Ellipse2D.Double(5, cy - 4, 8, 8));
            } else if (vorhanden) {
                g2.setColor(Ui.GRUEN);
                g2.fill(new Ellipse2D.Double(0, cy - 11, 22, 22));
                Ui.symbol(g2, Ui.Sym.HAKEN, 3, cy - 8, 16, Color.WHITE);
            } else {
                g2.setColor(Ui.ROT);
                g2.setStroke(new java.awt.BasicStroke(2f));
                g2.draw(new Ellipse2D.Double(1, cy - 10, 20, 20));
            }

            // rechts: Menge und ggf. "fehlt"
            int rechts = w;
            if (aktiv && !vorhanden) {
                g2.setFont(Ui.schrift(12f, Font.BOLD));
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth("fehlt") + 20;
                g2.setColor(Ui.mit(Ui.ROT, 40));
                g2.fillRoundRect(w - tw, cy - 12, tw, 24, 24, 24);
                g2.setColor(Ui.ROT);
                Ui.textMitte(g2, "fehlt", w - tw / 2, cy);
                rechts = w - tw - 12;
            }
            g2.setFont(Ui.schrift(15f, Font.PLAIN));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(Ui.gedimmt());
            g2.drawString(menge, rechts - fm.stringWidth(menge), cy - fm.getHeight() / 2 + fm.getAscent());

            // Name
            g2.setFont(Ui.schrift(16f, Font.PLAIN));
            fm = g2.getFontMetrics();
            g2.setColor(Ui.text());
            int maxName = rechts - fm.stringWidth(menge) - 40 - 34;
            g2.drawString(Ui.kuerze(name, fm, Math.max(60, maxName)), 36, cy - fm.getHeight() / 2 + fm.getAscent());

            if (trennlinie) {
                g2.setColor(Ui.rand());
                g2.drawLine(0, h - 1, w, h - 1);
            }
            g2.dispose();
        }
    }
}
