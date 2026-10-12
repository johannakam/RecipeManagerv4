package recipemanager;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Merkt sich den Verlauf in der lokalen Datei verlauf.json:
 * die zuletzt angesehenen Rezepte (ids) und die zuletzt gesuchten Zutaten-Kombinationen.
 */
public class VerlaufRepository {

    static final int MAX_REZEPTE = 5;
    static final int MAX_SUCHEN = 5;

    /** Inhalt der Datei. */
    static class Daten {
        List<Integer> rezepte = new ArrayList<>();
        List<List<String>> suchen = new ArrayList<>();
    }

    private final Path datei;
    private final Gson gson = new Gson();
    private Daten daten = new Daten();

    public VerlaufRepository(Path datei) {
        this.datei = datei;
        if (Files.exists(datei)) {
            try (Reader r = Files.newBufferedReader(datei, StandardCharsets.UTF_8)) {
                Daten aus = gson.fromJson(r, Daten.class);
                if (aus != null) {
                    if (aus.rezepte != null) daten.rezepte = new ArrayList<>(aus.rezepte);
                    if (aus.suchen != null) daten.suchen = new ArrayList<>(aus.suchen);
                }
            } catch (Exception e) {
                // kaputte Datei: einfach mit leerem Verlauf starten
            }
        }
    }

    /** Zuletzt angesehene Rezept-ids, neueste zuerst. */
    public List<Integer> rezepte() { return List.copyOf(daten.rezepte); }

    /** Zuletzt gesuchte Zutaten-Kombinationen (normalisierte Namen), neueste zuerst. */
    public List<List<String>> suchen() { return List.copyOf(daten.suchen); }

    public boolean istLeer() { return daten.rezepte.isEmpty() && daten.suchen.isEmpty(); }

    public void rezeptAngesehen(int id) throws IOException {
        daten.rezepte.remove(Integer.valueOf(id));
        daten.rezepte.add(0, id);
        while (daten.rezepte.size() > MAX_REZEPTE) daten.rezepte.remove(daten.rezepte.size() - 1);
        schreiben();
    }

    public void entferneRezept(int id) throws IOException {
        if (daten.rezepte.remove(Integer.valueOf(id))) schreiben();
    }

    /** Merkt eine Suche; dieselbe Kombination rückt nur nach vorne. */
    public void gesucht(Set<String> zutaten) throws IOException {
        if (zutaten.isEmpty()) return;
        Set<String> neu = new LinkedHashSet<>(zutaten);
        daten.suchen.removeIf(s -> new LinkedHashSet<>(s).equals(neu));
        daten.suchen.add(0, new ArrayList<>(neu));
        while (daten.suchen.size() > MAX_SUCHEN) daten.suchen.remove(daten.suchen.size() - 1);
        schreiben();
    }

    public void loeschen() throws IOException {
        daten = new Daten();
        schreiben();
    }

    private void schreiben() throws IOException {
        Path ordner = datei.toAbsolutePath().getParent();
        Path tmp = ordner.resolve(datei.getFileName() + ".tmp");
        Files.writeString(tmp, gson.toJson(daten), StandardCharsets.UTF_8);
        Files.move(tmp, datei, StandardCopyOption.REPLACE_EXISTING);
    }
}
