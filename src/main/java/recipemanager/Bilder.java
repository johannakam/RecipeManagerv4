package recipemanager;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Lädt die optionalen Fotos aus dem Ordner bilder/ (neben rezepte.json).
 * Ein Rezept mit dem Bild-Namen "ofenlachs" sucht ofenlachs.jpg, ofenlachs.jpeg oder ofenlachs.png.
 * Fehlt die Datei, liefert die Klasse null und die App zeigt stattdessen den bunten Buchstaben-Kreis.
 */
public final class Bilder {

    private Bilder() {
    }

    static Path ordner = Path.of("bilder");
    private static final String[] ENDUNGEN = {"jpg", "jpeg", "png"};
    private static final int MAX_BREITE = 1100;   // größere Fotos werden beim Laden verkleinert (spart Speicher)
    private static final Map<String, BufferedImage> VORSCHAU = new HashMap<>();
    private static final BufferedImage FEHLT = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    /** Dateiname aus dem Titel: "Gemüse-Lasagne" -> "gemuese-lasagne". */
    public static String slug(String titel) {
        String s = titel == null ? "" : titel.toLowerCase()
                .replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("ß", "ss");
        s = s.replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
        return s.isEmpty() ? "rezept" : s;
    }

    private static Path datei(String name) {
        for (String e : ENDUNGEN) {
            Path p = ordner.resolve(name + "." + e);
            if (Files.isRegularFile(p)) return p;
        }
        return null;
    }

    /** Kleines quadratisches Vorschaubild für die Listen (wird zwischengespeichert), oder null. */
    public static BufferedImage vorschau(String name) {
        BufferedImage b = VORSCHAU.get(name);
        if (b == null) {
            BufferedImage gross = lade(name, 400);
            b = gross == null ? FEHLT : zuschneiden(gross, 160);
            VORSCHAU.put(name, b);
        }
        return b == FEHLT ? null : b;
    }

    /** Großes Bild für die Detailseite, oder null. */
    public static BufferedImage gross(String name) {
        return lade(name, MAX_BREITE);
    }

    /** Vergisst ein zwischengespeichertes Vorschaubild (nach Ändern des Bildes). */
    public static void vergiss(String name) {
        VORSCHAU.remove(name);
    }

    /** Kopiert ein vom Nutzer gewähltes Foto in den Ordner bilder/ und gibt den Bild-Namen zurück. */
    public static String uebernehme(Path quelle, String name) throws IOException {
        Files.createDirectories(ordner);
        String datei = quelle.getFileName().toString().toLowerCase();
        String endung = datei.endsWith(".png") ? "png" : "jpg";
        for (String e : ENDUNGEN) Files.deleteIfExists(ordner.resolve(name + "." + e));
        Files.copy(quelle, ordner.resolve(name + "." + endung), StandardCopyOption.REPLACE_EXISTING);
        vergiss(name);
        return name;
    }

    private static BufferedImage lade(String name, int maxBreite) {
        if (name == null || name.isBlank()) return null;
        Path p = datei(name);
        if (p == null) return null;
        try (ImageInputStream in = ImageIO.createImageInputStream(p.toFile())) {
            Iterator<ImageReader> it = ImageIO.getImageReaders(in);
            if (!it.hasNext()) return null;
            ImageReader reader = it.next();
            try {
                reader.setInput(in);
                int teiler = Math.max(1, reader.getWidth(0) / maxBreite);   // große Fotos grob verkleinert lesen
                var param = reader.getDefaultReadParam();
                param.setSourceSubsampling(teiler, teiler, 0, 0);
                return reader.read(0, param);
            } finally {
                reader.dispose();
            }
        } catch (IOException | RuntimeException e) {
            return null;   // kaputte Datei: einfach den Buchstaben-Kreis zeigen
        }
    }

    private static BufferedImage zuschneiden(BufferedImage q, int d) {
        BufferedImage z = new BufferedImage(d, d, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = z.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        Ui.zeichneZugeschnitten(g, q, 0, 0, d, d);
        g.dispose();
        return z;
    }
}
