package recipemanager;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        Ui.installDefaults();   // runde Bedienelemente und Akzentfarbe (vor dem Setup)
        FlatLightLaf.setup();   // Start im hellen Modus, umschaltbar oben rechts

        Path datei = Path.of("rezepte.json");
        AppPanel app;
        try {
            app = new AppPanel(new RezeptRepository(datei), new FavoritenRepository(Path.of("favoriten.json")),
                    new VerlaufRepository(Path.of("verlauf.json")));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Die Rezepte konnten nicht gelesen werden:\n" + e.getMessage(),
                    "Fehler", JOptionPane.ERROR_MESSAGE);
            return;
        }


        if (!Files.exists(datei)) {
            JOptionPane.showMessageDialog(null,
                    "Die Datei rezepte.json wurde nicht gefunden:\n" + datei.toAbsolutePath()
                            + "\n\nLege sie in diesen Ordner (den Projektordner) und starte die App neu.",
                    "Hinweis", JOptionPane.INFORMATION_MESSAGE);
        }

        AppPanel fertig = app;
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Recipe Manager v4");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setContentPane(fertig);
            int hoehe = Math.min(900, GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height - 30);
            f.setSize(640, hoehe);
            f.setMinimumSize(new Dimension(600, 560));
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
