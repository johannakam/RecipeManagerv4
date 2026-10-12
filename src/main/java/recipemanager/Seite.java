package recipemanager;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Graphics;

/** Basis aller Bildschirme: Seitenhintergrund und eine zentrierte Inhaltsspalte (wie ein Handy-Display). */
abstract class Seite extends JPanel {

    Seite() {
        super(new GridBagLayout());
        setOpaque(true);
    }

    protected void setzeSpalte(JComponent spalte) {
        spalte.setPreferredSize(new Dimension(Ui.SPALTE, 100));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.VERTICAL;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTH;
        add(spalte, c);
    }

    /** Komponenten nebeneinander mit festem Abstand (linksbündig, ohne Streckung). */
    protected static JPanel reihe(int abstand, JComponent... komponenten) {
        JPanel p = Ui.leer(null);
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        for (int i = 0; i < komponenten.length; i++) {
            if (i > 0) p.add(Box.createHorizontalStrut(abstand));
            p.add(komponenten[i]);
        }
        p.add(Box.createHorizontalGlue());
        p.setAlignmentX(LEFT_ALIGNMENT);
        return p;
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Ui.seite());
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}
