package recipemanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Liest die Rezepte aus einer JSON-Datei (Standard: rezepte.json im Projektordner)
 * und schreibt Änderungen (neu, bearbeiten, löschen) wieder in dieselbe Datei zurück.
 * Alle Rezepte (weniger als 100) werden dafür einmal komplett im Speicher gehalten.
 */
public class RezeptRepository {

    private final Path datei;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private List<Rezept> rezepte;   // Zwischenspeicher, wird beim ersten Zugriff aus der Datei geladen

    public RezeptRepository() {
        this(Path.of("rezepte.json"));
    }

    public RezeptRepository(Path datei) {
        this.datei = datei;
    }

    /** Alle Rezepte, alphabetisch nach Titel sortiert. */
    public List<Rezept> findAll() throws IOException {
        List<Rezept> kopie = new ArrayList<>(laden());
        kopie.sort(Comparator.comparing(Rezept::getTitel, String.CASE_INSENSITIVE_ORDER));
        return kopie;
    }

    /** Legt das Rezept neu an (id == 0) oder ersetzt das vorhandene mit derselben id. */
    public void speichere(Rezept r) throws IOException {
        List<Rezept> liste = laden();
        if (r.getId() == 0) {
            r.setId(naechsteId(liste));
            liste.add(r);
        } else {
            boolean ersetzt = false;
            for (int i = 0; i < liste.size(); i++) {
                if (liste.get(i).getId() == r.getId()) {
                    liste.set(i, r);
                    ersetzt = true;
                    break;
                }
            }
            if (!ersetzt) liste.add(r);
        }
        schreiben(liste);
    }

    public void loesche(int id) throws IOException {
        List<Rezept> liste = laden();
        liste.removeIf(r -> r.getId() == id);
        schreiben(liste);
    }

    public int anzahl() throws IOException {
        return laden().size();
    }

    public List<String> alleKategorien() throws IOException {
        Map<String, String> namen = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Rezept r : laden()) {
            if (!r.getKategorie().isBlank()) namen.putIfAbsent(r.getKategorie(), r.getKategorie());
        }
        return new ArrayList<>(namen.values());
    }

    public List<String> alleZutatenNamen() throws IOException {
        Map<String, String> namen = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Rezept r : laden()) {
            for (Zutat z : r.getZutaten()) namen.putIfAbsent(z.name(), z.name());
        }
        return new ArrayList<>(namen.values());
    }

    // ---------------------------------------------------------------- intern

    private List<Rezept> laden() throws IOException {
        if (rezepte != null) return rezepte;

        List<Rezept> geladen = new ArrayList<>();
        if (Files.exists(datei)) {
            try (Reader reader = Files.newBufferedReader(datei, StandardCharsets.UTF_8)) {
                Type typ = new TypeToken<List<Rezept>>() { }.getType();
                List<Rezept> aus = gson.fromJson(reader, typ);
                if (aus != null) geladen = new ArrayList<>(aus);
            } catch (JsonParseException e) {
                throw new IOException("Die Datei " + datei.toAbsolutePath()
                        + " enthält ungültiges JSON: " + e.getMessage(), e);
            }
        }
        bereinige(geladen);
        rezepte = geladen;
        return rezepte;
    }

    /** Macht fehlende Felder in der Datei unschädlich (null -> leer) und vergibt fehlende ids. */
    private void bereinige(List<Rezept> liste) {
        int naechste = naechsteId(liste);
        for (Rezept r : liste) {
            if (r.getId() == 0) r.setId(naechste++);
            if (r.getTitel() == null) r.setTitel("Ohne Titel");
            if (r.getKategorie() == null) r.setKategorie("");
            if (r.getSchwierigkeitsgrad() == null || r.getSchwierigkeitsgrad().isBlank()) {
                r.setSchwierigkeitsgrad(Rezept.SCHWIERIGKEITEN[0]);
            }
            if (r.getAnleitung() == null) r.setAnleitung("");
            if (r.getPortionen() < 1) r.setPortionen(1);
            List<Zutat> sauber = new ArrayList<>();
            if (r.getZutaten() != null) {
                for (Zutat z : r.getZutaten()) {
                    if (z != null && z.name() != null && !z.name().isBlank()) {
                        sauber.add(new Zutat(z.name().trim(), z.menge(), z.einheit() == null ? "" : z.einheit()));
                    }
                }
            }
            r.setZutaten(sauber);
        }
    }

    private int naechsteId(List<Rezept> liste) {
        int max = 0;
        for (Rezept r : liste) max = Math.max(max, r.getId());
        return max + 1;
    }

    /** Schreibt zuerst in eine Hilfsdatei und ersetzt dann die echte Datei (schützt vor halb geschriebenen Dateien). */
    private void schreiben(List<Rezept> liste) throws IOException {
        Path ordner = datei.toAbsolutePath().getParent();
        Path tmp = ordner.resolve(datei.getFileName() + ".tmp");
        Files.writeString(tmp, gson.toJson(liste), StandardCharsets.UTF_8);
        Files.move(tmp, datei, StandardCopyOption.REPLACE_EXISTING);
    }
}
