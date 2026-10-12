package recipemanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Ordnet Zutaten für die Auswahlseite in Gruppen ein. Unbekannte Zutaten landen in "Sonstiges". */
public final class ZutatenKatalog {

    private ZutatenKatalog() {
    }

    public static final String SONSTIGES = "Sonstiges";

    /** Gruppen in der Reihenfolge, in der sie angezeigt werden. */
    private static final Map<String, List<String>> GRUPPEN = new LinkedHashMap<>();
    private static final Map<String, String> ZUORDNUNG = new HashMap<>();

    static {
        gruppe("Eiweiß", "Eier", "Eiklar", "Hähnchenbrust", "Putenbrust", "Putenhack", "Rinderhack", "Rindersteak",
                "Lachsfilet", "Räucherlachs", "Seelachsfilet", "Kabeljaufilet", "Garnelen", "Thunfisch", "Tofu",
                "Edamame", "Proteinpulver");
        gruppe("Milchprodukte", "Skyr", "Magerquark", "Hüttenkäse", "Griechischer Joghurt", "Naturjoghurt",
                "Milch", "Feta", "Mozzarella", "Parmesan", "Frischkäse");
        gruppe("Gemüse", "Tomaten", "Passierte Tomaten", "Gurke", "Paprika", "Zucchini", "Aubergine", "Brokkoli",
                "Blumenkohl", "Karotten", "Babyspinat", "Blattsalat", "Romanasalat", "Rucola", "Pak Choi",
                "Champignons", "Zwiebel", "Rote Zwiebel", "Knoblauch", "Avocado", "Süßkartoffel", "Kartoffeln",
                "Kürbis", "Rote Bete", "Spargel", "Fenchel", "Staudensellerie", "Erbsen", "Mais", "Ingwer");
        gruppe("Obst", "Banane", "Beeren", "Himbeeren", "Blaubeeren", "Erdbeeren", "Mango", "Apfel", "Zitrone",
                "Limette");
        gruppe("Getreide & Hülsenfrüchte", "Haferflocken", "Vollkornreis", "Risottoreis", "Quinoa", "Bulgur",
                "Couscous", "Vollkornnudeln", "Reisnudeln", "Lasagneblätter", "Vollkornbrot", "Vollkorn-Wrap",
                "Vollkornmehl", "Kichererbsen", "Kidneybohnen", "Rote Linsen", "Linsen");
        gruppe("Nüsse & Samen", "Walnüsse", "Mandeln", "Pinienkerne", "Chiasamen", "Leinsamen", "Sonnenblumenkerne",
                "Kürbiskerne", "Sesam", "Erdnussmus", "Tahin");
        gruppe("Kräuter", "Basilikum", "Petersilie", "Schnittlauch", "Dill", "Koriander");
        gruppe(SONSTIGES, "Honig", "Sojasauce", "Senf", "Tomatenmark", "Gemüsebrühe", "Kokosmilch", "Rote Currypaste",
                "Zitronensaft", "Kakao", "Dunkle Schokolade", "Hummus", "Kaffee");
    }

    private static void gruppe(String name, String... zutaten) {
        GRUPPEN.put(name, new ArrayList<>());
        for (String z : zutaten) ZUORDNUNG.put(RezeptService.normalisiere(z), name);
    }

    /** Namen der Gruppen in Anzeige-Reihenfolge. */
    public static List<String> gruppenNamen() {
        return new ArrayList<>(GRUPPEN.keySet());
    }

    public static String gruppeVon(String zutat) {
        return ZUORDNUNG.getOrDefault(RezeptService.normalisiere(zutat), SONSTIGES);
    }
}
