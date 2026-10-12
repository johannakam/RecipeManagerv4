package recipemanager;

import com.formdev.flatlaf.FlatLaf;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.HashMap;
import java.util.Map;

/**
 * Gemeinsames Design der App: Farben, Schriften und kleine selbst gezeichnete Bausteine
 * (Karten, Chips, Buttons, Symbole), damit die Oberfläche wie eine moderne Mobile-App aussieht.
 * Alle Farben werden beim Zeichnen abgefragt, damit der Wechsel Hell/Dunkel sofort wirkt.
 */
public final class Ui {

    private Ui() {
    }

    public static final Color ACCENT = new Color(0x12A37F);
    public static final Color GRUEN = new Color(0x37B24D);
    public static final Color ROT = new Color(0xE03131);
    public static final Color FAVORIT = new Color(0xE64980);

    /** Breite der Inhaltsspalte (wie ein Handy-Display, zentriert im Fenster). */
    public static final int SPALTE = 540;

    private static final Color[] PALETTE = {
            new Color(0xF76707), new Color(0x2F9E44), new Color(0x1C7ED6), new Color(0xAE3EC9),
            new Color(0xE03131), new Color(0x0CA678), new Color(0x5F3DC4), new Color(0xD6336C)
    };

    /** Stabile Farbe für einen Text (z. B. Kategorie), damit gleiche Kategorien gleich aussehen. */
    public static Color farbeFuer(String text) {
        int h = text == null ? 0 : Math.abs(RezeptService.normalisiere(text).hashCode() % PALETTE.length);
        return PALETTE[h];
    }

    // ---------------------------------------------------------------- Farben (hell/dunkel)

    public static boolean dunkel() { return FlatLaf.isLafDark(); }
    public static Color seite()    { return dunkel() ? new Color(0x121516) : new Color(0xF5F8F7); }
    public static Color karte()    { return dunkel() ? new Color(0x1C2022) : Color.WHITE; }
    public static Color rand()     { return dunkel() ? new Color(0x2C3235) : new Color(0xE2E8E5); }
    public static Color gedimmt()  { return dunkel() ? new Color(0x9AA4A0) : new Color(0x6B7773); }
    public static Color text()     { return dunkel() ? new Color(0xECF1EF) : new Color(0x1A2421); }

    public static Color mit(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    /** Rundere Standard-Bedienelemente und Akzentfarbe für FlatLaf. Muss vor dem Setup aufgerufen werden. */
    public static void installDefaults() {
        Map<String, String> d = new HashMap<>();
        d.put("@accentColor", "#12A37F");
        d.put("Button.arc", "999");
        d.put("Component.arc", "16");
        d.put("TextComponent.arc", "16");
        d.put("ScrollBar.thumbArc", "999");
        d.put("ScrollBar.thumbInsets", "2,2,2,2");
        d.put("ScrollBar.width", "12");
        d.put("TextField.margin", "6,12,6,12");
        FlatLaf.setGlobalExtraDefaults(d);
    }

    // ---------------------------------------------------------------- Hilfsmethoden

    static Graphics2D g2(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g2;
    }

    public static Font schrift(float groesse, int stil) {
        return UIManager.getFont("Label.font").deriveFont(stil, groesse);
    }

    /** Durchsichtiges Panel. */
    public static JPanel leer(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setOpaque(false);
        return p;
    }

    public static JLabel label(String text, float groesse, int stil) {
        JLabel l = new JLabel(text) {
            @Override
            public Color getForeground() {
                return text();   // folgt immer dem aktuellen Design (hell/dunkel)
            }
        };
        l.setFont(l.getFont().deriveFont(stil, groesse));
        return l;
    }

    /** Label in der gedämpften Zweitfarbe (nur einzeilig verwenden). */
    public static class MutedLabel extends JLabel {
        public MutedLabel(String text) {
            super(text);
            setFont(getFont().deriveFont(Font.PLAIN, 14f));
        }

        @Override
        public Color getForeground() {
            return gedimmt();
        }
    }

    /** Text mit Zeilenumbruch bei fester Breite (Farben folgen dem aktuellen Design). */
    public static JComponent umbruch(String text, int breite, float groesse, int stil) {
        return new TextBlock(text, breite, groesse, stil);
    }

    /** Mehrzeiliger Text, der bei fester Breite umbricht und seine Höhe selbst berechnet. */
    public static class TextBlock extends JComponent {
        private final String text;
        private final int breite;

        public TextBlock(String text, int breite, float groesse, int stil) {
            this.text = text == null ? "" : text;
            this.breite = breite;
            setFont(schrift(groesse, stil));
            setOpaque(false);
        }

        private java.util.List<String> zeilen(FontMetrics fm, int maxBreite) {
            java.util.List<String> zeilen = new java.util.ArrayList<>();
            String zeile = "";
            for (String wort : text.split("\\s+")) {
                String test = zeile.isEmpty() ? wort : zeile + " " + wort;
                if (zeile.isEmpty() || fm.stringWidth(test) <= maxBreite) {
                    zeile = test;
                } else {
                    zeilen.add(zeile);
                    zeile = wort;
                }
            }
            zeilen.add(zeile);
            return zeilen;
        }

        @Override
        public Dimension getPreferredSize() {
            java.awt.Insets in = getInsets();
            FontMetrics fm = getFontMetrics(getFont());
            int n = zeilen(fm, breite).size();
            return new Dimension(breite + in.left + in.right, n * fm.getHeight() + 4 + in.top + in.bottom);
        }

        @Override public Dimension getMinimumSize() { return getPreferredSize(); }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = g2(g);
            java.awt.Insets in = getInsets();
            g2.setFont(getFont());
            g2.setColor(text());
            FontMetrics fm = g2.getFontMetrics();
            int y = in.top + fm.getAscent() + 2;
            for (String z : zeilen(fm, breite)) {
                g2.drawString(z, in.left, y);
                y += fm.getHeight();
            }
            g2.dispose();
        }
    }

    public static <T extends JComponent> T links(T c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    public static Border abstand(int oben, int links, int unten, int rechts) {
        return BorderFactory.createEmptyBorder(oben, links, unten, rechts);
    }

    /** ScrollPane ohne Rahmen mit durchsichtigem Hintergrund. */
    public static JScrollPane scroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(null);
        sp.setViewportBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(20);
        return sp;
    }

    public static String kuerze(String s, FontMetrics fm, int maxBreite) {
        if (s == null) return "";
        if (fm.stringWidth(s) <= maxBreite) return s;
        String punkte = "…";
        int n = s.length();
        while (n > 0 && fm.stringWidth(s.substring(0, n) + punkte) > maxBreite) n--;
        return s.substring(0, n) + punkte;
    }

    /** Runder Avatar mit dem Anfangsbuchstaben. */
    public static void zeichneAvatar(Graphics2D g, int x, int y, int d, Color farbe, String text) {
        g.setColor(mit(ACCENT, dunkel() ? 50 : 36));
        g.fill(new Ellipse2D.Double(x, y, d, d));
        String b = (text == null || text.isBlank()) ? "?" : text.trim().substring(0, 1).toUpperCase();
        g.setColor(ACCENT);
        g.setFont(schrift(d * 0.42f, Font.BOLD));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(b, x + (d - fm.stringWidth(b)) / 2, y + (d - fm.getHeight()) / 2 + fm.getAscent());
    }

    /** Zeichnet ein Bild rund (Mitte zugeschnitten) in das Quadrat x,y,d. */
    public static void zeichneRundesBild(Graphics2D g, java.awt.image.BufferedImage bild, int x, int y, int d) {
        java.awt.Shape alt = g.getClip();
        g.clip(new java.awt.geom.RoundRectangle2D.Double(x, y, d, d, d * 0.3, d * 0.3));
        zeichneZugeschnitten(g, bild, x, y, d, d);
        g.setClip(alt);
    }

    /** Füllt das Rechteck mit dem Bild (Seitenverhältnis bleibt, Überstand wird abgeschnitten). */
    public static void zeichneZugeschnitten(Graphics2D g, java.awt.image.BufferedImage bild, int x, int y, int w, int h) {
        double f = Math.max(w / (double) bild.getWidth(), h / (double) bild.getHeight());
        int bw = (int) Math.ceil(bild.getWidth() * f), bh = (int) Math.ceil(bild.getHeight() * f);
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(bild, x + (w - bw) / 2, y + (h - bh) / 2, bw, bh, null);
    }

    public static void textMitte(Graphics2D g, String s, int mitteX, int mitteY) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, mitteX - fm.stringWidth(s) / 2, mitteY - fm.getHeight() / 2 + fm.getAscent());
    }

    // ---------------------------------------------------------------- Symbole (selbst gezeichnet)

    public enum Sym { SONNE, APFEL, TELLER, LISTE, ZURUECK, PFEIL, MOND, UHR, PLUS, MINUS, HAKEN, TASSE, BESTECK, HERZ, HERZ_VOLL, STIFT, PAPIERKORB }

    public static void symbol(Graphics2D g, Sym sym, double x, double y, double s, Color farbe) {
        g.setColor(farbe);
        g.setStroke(new BasicStroke((float) Math.max(1.7, s * 0.085), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        double cx = x + s / 2, cy = y + s / 2;
        switch (sym) {
            case SONNE -> {
                double r = s * 0.19;
                g.draw(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
                for (int i = 0; i < 8; i++) {
                    double a = Math.PI / 4 * i;
                    g.draw(new Line2D.Double(cx + Math.cos(a) * s * 0.30, cy + Math.sin(a) * s * 0.30,
                            cx + Math.cos(a) * s * 0.44, cy + Math.sin(a) * s * 0.44));
                }
            }
            case APFEL -> {
                Path2D p = new Path2D.Double();
                p.moveTo(cx, y + s * 0.30);
                p.curveTo(x + s * 0.62, y + s * 0.20, x + s * 0.92, y + s * 0.38, x + s * 0.88, y + s * 0.60);
                p.curveTo(x + s * 0.84, y + s * 0.80, x + s * 0.66, y + s * 0.92, cx, y + s * 0.86);
                p.curveTo(x + s * 0.34, y + s * 0.92, x + s * 0.16, y + s * 0.80, x + s * 0.12, y + s * 0.60);
                p.curveTo(x + s * 0.08, y + s * 0.38, x + s * 0.38, y + s * 0.20, cx, y + s * 0.30);
                g.draw(p);
                g.draw(new Line2D.Double(cx, y + s * 0.30, cx + s * 0.04, y + s * 0.12));
                Path2D blatt = new Path2D.Double();
                blatt.moveTo(cx + s * 0.05, y + s * 0.20);
                blatt.curveTo(cx + s * 0.14, y + s * 0.05, cx + s * 0.28, y + s * 0.05, cx + s * 0.31, y + s * 0.09);
                blatt.curveTo(cx + s * 0.28, y + s * 0.22, cx + s * 0.14, y + s * 0.24, cx + s * 0.05, y + s * 0.20);
                g.draw(blatt);
            }
            case TASSE -> {
                Path2D k = new Path2D.Double();
                k.moveTo(x + s * 0.14, y + s * 0.44);
                k.lineTo(x + s * 0.14, y + s * 0.60);
                k.curveTo(x + s * 0.14, y + s * 0.80, x + s * 0.28, y + s * 0.90, x + s * 0.42, y + s * 0.90);
                k.curveTo(x + s * 0.56, y + s * 0.90, x + s * 0.70, y + s * 0.80, x + s * 0.70, y + s * 0.60);
                k.lineTo(x + s * 0.70, y + s * 0.44);
                k.closePath();
                g.draw(k);
                Path2D henkel = new Path2D.Double();
                henkel.moveTo(x + s * 0.70, y + s * 0.50);
                henkel.curveTo(x + s * 0.94, y + s * 0.46, x + s * 0.94, y + s * 0.76, x + s * 0.64, y + s * 0.74);
                g.draw(henkel);
                for (double dx : new double[] {0.28, 0.50}) {
                    Path2D dampf = new Path2D.Double();
                    dampf.moveTo(x + s * dx, y + s * 0.32);
                    dampf.curveTo(x + s * (dx - 0.07), y + s * 0.25, x + s * (dx + 0.07), y + s * 0.18, x + s * dx, y + s * 0.09);
                    g.draw(dampf);
                }
            }
            case BESTECK -> {
                Path2D gabel = new Path2D.Double();
                gabel.moveTo(x + s * 0.20, y + s * 0.10);
                gabel.lineTo(x + s * 0.20, y + s * 0.34);
                gabel.curveTo(x + s * 0.20, y + s * 0.48, x + s * 0.44, y + s * 0.48, x + s * 0.44, y + s * 0.34);
                gabel.lineTo(x + s * 0.44, y + s * 0.10);
                g.draw(gabel);
                g.draw(new Line2D.Double(x + s * 0.32, y + s * 0.10, x + s * 0.32, y + s * 0.40));
                g.draw(new Line2D.Double(x + s * 0.32, y + s * 0.46, x + s * 0.32, y + s * 0.90));
                Path2D messer = new Path2D.Double();
                messer.moveTo(x + s * 0.72, y + s * 0.90);
                messer.lineTo(x + s * 0.72, y + s * 0.10);
                messer.curveTo(x + s * 0.92, y + s * 0.22, x + s * 0.92, y + s * 0.54, x + s * 0.72, y + s * 0.58);
                g.draw(messer);
            }
            case TELLER -> {
                double r = s * 0.44, r2 = s * 0.25;
                g.draw(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
                g.draw(new Ellipse2D.Double(cx - r2, cy - r2, 2 * r2, 2 * r2));
            }
            case LISTE -> {
                for (int i = 0; i < 3; i++) {
                    double yy = y + s * (0.25 + 0.25 * i);
                    g.fill(new Ellipse2D.Double(x + s * 0.12, yy - s * 0.045, s * 0.09, s * 0.09));
                    g.draw(new Line2D.Double(x + s * 0.33, yy, x + s * 0.88, yy));
                }
            }
            case ZURUECK -> {
                Path2D p = new Path2D.Double();
                p.moveTo(x + s * 0.62, y + s * 0.2);
                p.lineTo(x + s * 0.34, y + s * 0.5);
                p.lineTo(x + s * 0.62, y + s * 0.8);
                g.draw(p);
            }
            case PFEIL -> {
                Path2D p = new Path2D.Double();
                p.moveTo(x + s * 0.38, y + s * 0.2);
                p.lineTo(x + s * 0.66, y + s * 0.5);
                p.lineTo(x + s * 0.38, y + s * 0.8);
                g.draw(p);
            }
            case MOND -> {
                Area a = new Area(new Ellipse2D.Double(x + s * 0.16, y + s * 0.18, s * 0.68, s * 0.68));
                a.subtract(new Area(new Ellipse2D.Double(x + s * 0.38, y + s * 0.04, s * 0.62, s * 0.62)));
                g.fill(a);
            }
            case UHR -> {
                double r = s * 0.42;
                g.draw(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
                g.draw(new Line2D.Double(cx, cy, cx, cy - s * 0.24));
                g.draw(new Line2D.Double(cx, cy, cx + s * 0.17, cy + s * 0.09));
            }
            case PLUS -> {
                g.draw(new Line2D.Double(cx - s * 0.26, cy, cx + s * 0.26, cy));
                g.draw(new Line2D.Double(cx, cy - s * 0.26, cx, cy + s * 0.26));
            }
            case MINUS -> g.draw(new Line2D.Double(cx - s * 0.26, cy, cx + s * 0.26, cy));
            case HERZ, HERZ_VOLL -> {
                Path2D p = new Path2D.Double();
                p.moveTo(cx, y + s * 0.88);
                p.curveTo(x + s * 0.02, y + s * 0.56, x + s * 0.08, y + s * 0.14, x + s * 0.30, y + s * 0.16);
                p.curveTo(x + s * 0.42, y + s * 0.17, cx, y + s * 0.26, cx, y + s * 0.34);
                p.curveTo(cx, y + s * 0.26, x + s * 0.58, y + s * 0.17, x + s * 0.70, y + s * 0.16);
                p.curveTo(x + s * 0.92, y + s * 0.14, x + s * 0.98, y + s * 0.56, cx, y + s * 0.88);
                p.closePath();
                if (sym == Sym.HERZ_VOLL) g.fill(p);
                g.draw(p);
            }
            case STIFT -> {
                g.draw(new Line2D.Double(x + s * 0.30, y + s * 0.70, x + s * 0.74, y + s * 0.26));
                g.draw(new Line2D.Double(x + s * 0.18, y + s * 0.86, x + s * 0.50, y + s * 0.86));
                g.draw(new Line2D.Double(x + s * 0.30, y + s * 0.70, x + s * 0.24, y + s * 0.80));
            }
            case PAPIERKORB -> {
                Path2D p = new Path2D.Double();
                p.moveTo(x + s * 0.26, y + s * 0.34);
                p.lineTo(x + s * 0.32, y + s * 0.88);
                p.lineTo(x + s * 0.68, y + s * 0.88);
                p.lineTo(x + s * 0.74, y + s * 0.34);
                g.draw(p);
                g.draw(new Line2D.Double(x + s * 0.16, y + s * 0.28, x + s * 0.84, y + s * 0.28));
                Path2D h = new Path2D.Double();
                h.moveTo(x + s * 0.40, y + s * 0.28);
                h.lineTo(x + s * 0.40, y + s * 0.14);
                h.lineTo(x + s * 0.60, y + s * 0.14);
                h.lineTo(x + s * 0.60, y + s * 0.28);
                g.draw(h);
            }
            case HAKEN -> {
                Path2D p = new Path2D.Double();
                p.moveTo(x + s * 0.22, y + s * 0.52);
                p.lineTo(x + s * 0.42, y + s * 0.72);
                p.lineTo(x + s * 0.80, y + s * 0.30);
                g.draw(p);
            }
        }
    }

    // ---------------------------------------------------------------- Bausteine

    /** Abgerundete Karte (weiß bzw. dunkelgrau, mit feinem Rand). */
    public static class Card extends JPanel {
        private final int arc;
        private Color fill;
        private Color rahmen;

        public Card(LayoutManager lm, int arc) {
            super(lm);
            this.arc = arc;
            setOpaque(false);
        }

        public Card setFarben(Color fill, Color rahmen) {
            this.fill = fill;
            this.rahmen = rahmen;
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = g2(g);
            g2.setColor(fill != null ? fill : karte());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.setColor(rahmen != null ? rahmen : rand());
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.dispose();
        }
    }

    /** Panel für ScrollPanes: folgt der Breite des Fensters, wird aber nach Bedarf höher. */
    public static class ScrollPanel extends JPanel implements Scrollable {
        public ScrollPanel(LayoutManager lm) {
            super(lm);
            setOpaque(false);
        }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 20; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) {
            return o == SwingConstants.VERTICAL ? Math.max(40, r.height - 40) : r.width;
        }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    /** Ordnet Komponenten in Zeilen an und bricht bei fester Breite um (für die Zutaten-Chips). */
    public static class Fluss implements LayoutManager {
        private final int breite, hgap, vgap;

        public Fluss(int breite, int hgap, int vgap) {
            this.breite = breite;
            this.hgap = hgap;
            this.vgap = vgap;
        }

        @Override public void addLayoutComponent(String name, Component comp) { }
        @Override public void removeLayoutComponent(Component comp) { }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            int x = 0, y = 0, zeilenHoehe = 0;
            for (Component c : parent.getComponents()) {
                if (!c.isVisible()) continue;
                Dimension d = c.getPreferredSize();
                if (x > 0 && x + d.width > breite) {
                    y += zeilenHoehe + vgap;
                    x = 0;
                    zeilenHoehe = 0;
                }
                x += d.width + hgap;
                zeilenHoehe = Math.max(zeilenHoehe, d.height);
            }
            return new Dimension(breite, y + zeilenHoehe);
        }

        @Override public Dimension minimumLayoutSize(Container parent) { return preferredLayoutSize(parent); }

        @Override
        public void layoutContainer(Container parent) {
            int max = parent.getWidth() > 0 ? parent.getWidth() : breite;
            int x = 0, y = 0, zeilenHoehe = 0;
            for (Component c : parent.getComponents()) {
                if (!c.isVisible()) continue;
                Dimension d = c.getPreferredSize();
                if (x > 0 && x + d.width > max) {
                    y += zeilenHoehe + vgap;
                    x = 0;
                    zeilenHoehe = 0;
                }
                c.setBounds(x, y, d.width, d.height);
                x += d.width + hgap;
                zeilenHoehe = Math.max(zeilenHoehe, d.height);
            }
        }
    }

    private static void standardButton(JButton b) {
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setRolloverEnabled(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** Runder Button mit Symbol (Zurück, Hell/Dunkel, Plus/Minus ...). */
    public static class IconButton extends JButton {
        private final Sym sym;
        private final boolean umrandet;

        public IconButton(Sym sym, boolean umrandet, Runnable aktion) {
            this.sym = sym;
            this.umrandet = umrandet;
            standardButton(this);
            setPreferredSize(new Dimension(40, 40));
            setMinimumSize(new Dimension(40, 40));
            setMaximumSize(new Dimension(40, 40));
            addActionListener(e -> aktion.run());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = g2(g);
            int w = getWidth(), h = getHeight();
            boolean hover = getModel().isRollover() || getModel().isArmed();
            if (umrandet) {
                g2.setColor(karte());
                g2.fillOval(0, 0, w - 1, h - 1);
                g2.setColor(rand());
                g2.drawOval(0, 0, w - 1, h - 1);
            }
            if (hover) {
                g2.setColor(mit(ACCENT, 34));
                g2.fillOval(0, 0, w - 1, h - 1);
            }
            double s = 22;
            symbol(g2, sym, (w - s) / 2, (h - s) / 2, s, hover ? ACCENT : text());
            g2.dispose();
        }
    }

    /** Herz zum Merken eines Rezepts (umschaltbar). */
    public static class HerzButton extends JButton {
        private boolean aktiv;

        public HerzButton(boolean aktiv, java.util.function.Consumer<Boolean> beiWechsel) {
            this.aktiv = aktiv;
            standardButton(this);
            setPreferredSize(new Dimension(44, 44));
            setMinimumSize(new Dimension(44, 44));
            setMaximumSize(new Dimension(44, 44));
            addActionListener(e -> {
                this.aktiv = !this.aktiv;
                repaint();
                beiWechsel.accept(this.aktiv);
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = g2(g);
            int w = getWidth(), h = getHeight();
            g2.setColor(karte());
            g2.fillOval(0, 0, w - 1, h - 1);
            g2.setColor(aktiv ? FAVORIT : rand());
            g2.drawOval(0, 0, w - 1, h - 1);
            if (getModel().isRollover()) {
                g2.setColor(mit(FAVORIT, 30));
                g2.fillOval(0, 0, w - 1, h - 1);
            }
            double sz = 24;
            symbol(g2, aktiv ? Sym.HERZ_VOLL : Sym.HERZ, (w - sz) / 2, (h - sz) / 2 + 1, sz, aktiv ? FAVORIT : text());
            g2.dispose();
        }
    }

    /** Runder Button in drei Stilen: gefüllt (Akzentfarbe), umrandet oder nur Text. */
    public static class PillButton extends JButton {
        public enum Stil { PRIMAER, SEKUNDAER, TEXT }

        private final Stil stil;

        public PillButton(String text, Stil stil, Runnable aktion) {
            super(text);
            this.stil = stil;
            standardButton(this);
            setFont(getFont().deriveFont(Font.BOLD, 14f));
            addActionListener(e -> aktion.run());
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(getText()) + 36, 42);
        }

        @Override public Dimension getMinimumSize() { return getPreferredSize(); }
        @Override public Dimension getMaximumSize() { return getPreferredSize(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = g2(g);
            int w = getWidth(), h = getHeight();
            boolean hover = isEnabled() && (getModel().isRollover() || getModel().isArmed());
            Color textFarbe;
            if (!isEnabled()) {
                g2.setColor(rand());
                g2.fillRoundRect(0, 0, w, h, h, h);
                textFarbe = gedimmt();
            } else if (stil == Stil.PRIMAER) {
                Color c = getModel().isPressed() ? ACCENT.darker()
                        : hover ? new Color(0x24B992) : ACCENT;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, w, h, h, h);
                textFarbe = Color.WHITE;
            } else {
                if (stil == Stil.SEKUNDAER) {
                    g2.setColor(karte());
                    g2.fillRoundRect(0, 0, w, h, h, h);
                }
                if (hover) {
                    g2.setColor(mit(ACCENT, 30));
                    g2.fillRoundRect(0, 0, w, h, h, h);
                }
                if (stil == Stil.SEKUNDAER) {
                    g2.setColor(rand());
                    g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);
                }
                textFarbe = stil == Stil.TEXT ? ACCENT : text();
            }
            g2.setFont(getFont());
            g2.setColor(textFarbe);
            textMitte(g2, getText(), w / 2, h / 2);
            g2.dispose();
        }
    }

    /** Auswahl-Chip (Filter, Zutaten). Ausgewählt = Akzentfarbe. */
    public static class Chip extends JToggleButton {
        public Chip(String text) {
            super(text);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(getFont().deriveFont(Font.BOLD, 13f));
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(getText()) + 30, 36);
        }

        @Override public Dimension getMinimumSize() { return getPreferredSize(); }
        @Override public Dimension getMaximumSize() { return getPreferredSize(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = g2(g);
            int w = getWidth(), h = getHeight();
            boolean hover = getModel().isRollover();
            if (isSelected()) {
                g2.setColor(ACCENT);
                g2.fillRoundRect(0, 0, w, h, h, h);
                g2.setColor(Color.WHITE);
            } else {
                g2.setColor(karte());
                g2.fillRoundRect(0, 0, w, h, h, h);
                if (hover) {
                    g2.setColor(mit(ACCENT, 30));
                    g2.fillRoundRect(0, 0, w, h, h, h);
                }
                g2.setColor(hover ? ACCENT : rand());
                g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);
                g2.setColor(text());
            }
            g2.setFont(getFont());
            textMitte(g2, getText(), w / 2, h / 2);
            g2.dispose();
        }
    }

    /** Kleines farbiges Etikett (Kategorie, Dauer, Schwierigkeit). */
    public static class Tag extends JComponent {
        private final String text;
        private final Color farbe;
        private final Sym sym;

        public Tag(String text, Color farbe, Sym sym) {
            this.text = text;
            this.farbe = farbe;
            this.sym = sym;
            setFont(schrift(13f, Font.BOLD));
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(text) + 26 + (sym != null ? 20 : 0), 30);
        }

        @Override public Dimension getMinimumSize() { return getPreferredSize(); }
        @Override public Dimension getMaximumSize() { return getPreferredSize(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = g2(g);
            int w = getWidth(), h = getHeight();
            g2.setColor(mit(farbe, dunkel() ? 60 : 34));
            g2.fillRoundRect(0, 0, w, h, h, h);
            g2.setColor(dunkel() ? farbe.brighter() : farbe.darker());
            g2.setFont(getFont());
            int tx = 13;
            if (sym != null) {
                symbol(g2, sym, 10, (h - 15) / 2.0, 15, g2.getColor());
                tx = 30;
            }
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, tx, (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Fortschrittsbalken (0 bis 1). */
    public static class Balken extends JComponent {
        private final double anteil;
        private final Color farbe;

        public Balken(double anteil, Color farbe) {
            this.anteil = Math.max(0, Math.min(1, anteil));
            this.farbe = farbe;
            setPreferredSize(new Dimension(100, 8));
        }

        @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 8); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = g2(g);
            int w = getWidth(), h = getHeight();
            g2.setColor(mit(text(), 28));
            g2.fillRoundRect(0, 0, w, h, h, h);
            if (anteil > 0) {
                g2.setColor(farbe);
                g2.fillRoundRect(0, 0, Math.max(h, (int) Math.round(w * anteil)), h, h, h);
            }
            g2.dispose();
        }
    }
}
