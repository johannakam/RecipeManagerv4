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
import java.awt.geom.Ellipse2D;

/**
 * Kachel der Startseite: schlichte Karte mit getöntem Symbol-Kreis, Titel und Untertitel.
 * Eine "betonte" Kachel (Hauptaktion) ist durchgehend in der Akzentfarbe gefüllt.
 */
class Kachel extends JComponent {

    private final String titel;
    private final String untertitel;
    private final Ui.Sym symbol;
    private final Color farbe;
    private final boolean betont;
    private boolean hover;

    Kachel(String titel, String untertitel, Ui.Sym symbol, Color farbe, boolean betont, Runnable aktion) {
        this.titel = titel;
        this.untertitel = untertitel;
        this.symbol = symbol;
        this.farbe = farbe;
        this.betont = betont;
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setAlignmentX(LEFT_ALIGNMENT);
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            @Override public void mouseClicked(MouseEvent e) { aktion.run(); }
        });
    }

    @Override public Dimension getPreferredSize() { return new Dimension(Ui.SPALTE, 92); }
    @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 92); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Ui.g2(g);
        int w = getWidth(), h = getHeight(), arc = 26;
        Color textFarbe, subFarbe, iconFarbe, kreis;
        if (betont) {
            g2.setColor(hover ? new Color(0x24B992) : farbe);
            g2.fillRoundRect(0, 0, w, h, arc, arc);
            textFarbe = Color.WHITE;
            subFarbe = new Color(255, 255, 255, 215);
            iconFarbe = Color.WHITE;
            kreis = new Color(255, 255, 255, 52);
        } else {
            g2.setColor(Ui.karte());
            g2.fillRoundRect(0, 0, w, h, arc, arc);
            g2.setColor(hover ? farbe : Ui.rand());
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
            textFarbe = Ui.text();
            subFarbe = Ui.gedimmt();
            iconFarbe = farbe;
            kreis = Ui.mit(farbe, Ui.dunkel() ? 46 : 30);
        }

        int d = 54, ix = 20, iy = (h - d) / 2;
        g2.setColor(kreis);
        g2.fill(new Ellipse2D.Double(ix, iy, d, d));
        Ui.symbol(g2, symbol, ix + 13, iy + 13, d - 26, iconFarbe);

        int tx = ix + d + 18;
        g2.setFont(Ui.schrift(18f, Font.BOLD));
        g2.setColor(textFarbe);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(Ui.kuerze(titel, fm, w - tx - 50), tx, h / 2 - 2);
        g2.setFont(Ui.schrift(13f, Font.PLAIN));
        g2.setColor(subFarbe);
        fm = g2.getFontMetrics();
        g2.drawString(Ui.kuerze(untertitel, fm, w - tx - 50), tx, h / 2 + 19);

        Ui.symbol(g2, Ui.Sym.PFEIL, w - 44, (h - 20) / 2.0, 20, betont ? Color.WHITE : Ui.gedimmt());
        g2.dispose();
    }
}
