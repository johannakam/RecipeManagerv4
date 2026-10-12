package recipemanager;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

/** Obere Leiste: links Zurück-Pfeil (optional), rechts Hell/Dunkel-Umschalter. */
class Kopfleiste extends JPanel {

    Kopfleiste(AppPanel app, boolean zurueck, String titel) {
        this(app, zurueck, titel, true);
    }

    /** designWechsel = false blendet den Hell/Dunkel-Knopf aus (im Formular, damit Eingaben nicht verloren gehen). */
    Kopfleiste(AppPanel app, boolean zurueck, String titel, boolean designWechsel) {
        super(new BorderLayout());
        setOpaque(false);
        setBorder(Ui.abstand(8, 0, 8, 0));
        setAlignmentX(LEFT_ALIGNMENT);

        JPanel links = Ui.leer(new FlowLayout(FlowLayout.LEFT, 0, 0));
        if (zurueck) links.add(new Ui.IconButton(Ui.Sym.ZURUECK, true, app::zurueck));
        else links.add(Box.createRigidArea(new Dimension(40, 40)));
        add(links, BorderLayout.WEST);

        if (titel != null) {
            JLabel t = Ui.label(titel, 17f, Font.BOLD);
            t.setBorder(Ui.abstand(0, 14, 0, 0));
            add(t, BorderLayout.CENTER);
        }

        if (designWechsel) add(new Ui.IconButton(Ui.dunkel() ? Ui.Sym.SONNE : Ui.Sym.MOND, true, app::wechsleDesign), BorderLayout.EAST);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
    }
}
