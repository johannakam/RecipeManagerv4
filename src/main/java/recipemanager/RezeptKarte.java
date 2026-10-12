package recipemanager;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Karte für ein Rezept in der Liste. Mit Treffer-Angabe (Zutaten-Matching) zeigt sie
 * Prozentzahl und Fortschrittsbalken, sonst einen Pfeil.
 */
class RezeptKarte extends JComponent {

    private final Rezept rezept;
    private final RezeptService.Treffer treffer;   // kann null sein
    private final boolean favorit;
    private final java.awt.image.BufferedImage foto;
    private boolean hover;

    RezeptKarte(Rezept rezept, RezeptService.Treffer treffer, boolean favorit, Runnable aktion) {
        this.rezept = rezept;
        this.favorit = favorit;
        this.foto = Bilder.vorschau(rezept.getBild());
        this.treffer = treffer;
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setAlignmentX(LEFT_ALIGNMENT);
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            @Override public void mouseClicked(MouseEvent e) { aktion.run(); }
        });
    }

    @Override public Dimension getPreferredSize() { return new Dimension(Ui.SPALTE, treffer != null ? 104 : 94); }
    @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, getPreferredSize().height); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Ui.g2(g);
        int w = getWidth(), h = getHeight(), arc = 26;

        g2.setColor(Ui.karte());
        g2.fillRoundRect(0, 0, w, h, arc, arc);
        g2.setColor(hover ? Ui.ACCENT : Ui.rand());
        g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

        String farbBasis = rezept.getKategorie().isBlank() ? rezept.getTitel() : rezept.getKategorie();
        int d = 64, ax = 14, ay = (h - d) / 2;
        if (foto != null) Ui.zeichneRundesBild(g2, foto, ax, ay, d);
        else Ui.zeichneAvatar(g2, ax, ay, d, Ui.farbeFuer(farbBasis), rezept.getTitel());

        int tx = ax + d + 14;
        int rechts = w - 18;
        String info = rezept.getZubereitungszeit() + " Min.  ·  " + rezept.getSchwierigkeitsgrad();

        if (treffer != null) {
            String prozent = treffer.prozent() + " %";
            g2.setFont(Ui.schrift(17f, Font.BOLD));
            g2.setColor(treffer.anteil() >= 1 ? Ui.GRUEN : Ui.ACCENT);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(prozent, rechts - fm.stringWidth(prozent), 33);
            int textRechts = rechts - fm.stringWidth(prozent) - 12;

            g2.setFont(Ui.schrift(17f, Font.BOLD));
            fm = g2.getFontMetrics();
            g2.setColor(Ui.text());
            g2.drawString(Ui.kuerze(rezept.getTitel(), fm, textRechts - tx), tx, 33);

            g2.setFont(Ui.schrift(13f, Font.PLAIN));
            fm = g2.getFontMetrics();
            g2.setColor(Ui.gedimmt());
            String zeile2 = treffer.vorhanden() + " von " + treffer.gesamt() + " Zutaten  ·  " + info;
            g2.drawString(Ui.kuerze(zeile2, fm, rechts - tx), tx, 55);

            int by = h - 26, bw = rechts - tx;
            g2.setColor(Ui.mit(Ui.text(), 28));
            g2.fillRoundRect(tx, by, bw, 8, 8, 8);
            if (treffer.anteil() > 0) {
                g2.setColor(treffer.anteil() >= 1 ? Ui.GRUEN : Ui.ACCENT);
                g2.fillRoundRect(tx, by, Math.max(8, (int) Math.round(bw * treffer.anteil())), 8, 8, 8);
            }
        } else {
            Ui.symbol(g2, Ui.Sym.PFEIL, w - 40, (h - 22) / 2.0, 22, Ui.gedimmt());
            int textRechts = w - 52;

            g2.setFont(Ui.schrift(17f, Font.BOLD));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(Ui.text());
            g2.drawString(Ui.kuerze(rezept.getTitel(), fm, textRechts - tx), tx, h / 2 - 3);

            g2.setFont(Ui.schrift(13f, Font.PLAIN));
            fm = g2.getFontMetrics();
            g2.setColor(Ui.gedimmt());
            g2.drawString(Ui.kuerze(info, fm, textRechts - tx), tx, h / 2 + 20);
        }
        if (favorit) {
            g2.setColor(Ui.karte());
            g2.fillOval(ax + d - 20, ay + d - 20, 22, 22);
            Ui.symbol(g2, Ui.Sym.HERZ_VOLL, ax + d - 17, ay + d - 17, 16, Ui.FAVORIT);
        }
        g2.dispose();
    }
}
