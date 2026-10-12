package recipemanager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Merkt sich die Favoriten (Rezept-ids) in der lokalen Datei favoriten.json. Funktioniert daher immer offline. */
public class FavoritenRepository {

    private final Path datei;
    private final Gson gson = new Gson();
    private final Set<Integer> ids = new LinkedHashSet<>();

    public FavoritenRepository(Path datei) {
        this.datei = datei;
        if (Files.exists(datei)) {
            try (Reader r = Files.newBufferedReader(datei, StandardCharsets.UTF_8)) {
                Type typ = new TypeToken<List<Integer>>() { }.getType();
                List<Integer> aus = gson.fromJson(r, typ);
                if (aus != null) ids.addAll(aus);
            } catch (Exception e) {
                // kaputte Datei: einfach ohne Favoriten starten
            }
        }
    }

    public boolean istFavorit(int id) { return ids.contains(id); }

    public int anzahl() { return ids.size(); }

    /** Setzt oder entfernt den Favoriten und speichert sofort. */
    public void setze(int id, boolean favorit) throws IOException {
        if (favorit) ids.add(id);
        else ids.remove(id);
        schreiben();
    }

    private void schreiben() throws IOException {
        Path ordner = datei.toAbsolutePath().getParent();
        Path tmp = ordner.resolve(datei.getFileName() + ".tmp");
        Files.writeString(tmp, gson.toJson(ids), StandardCharsets.UTF_8);
        Files.move(tmp, datei, StandardCopyOption.REPLACE_EXISTING);
    }
}
