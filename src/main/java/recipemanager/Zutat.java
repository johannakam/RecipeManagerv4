package recipemanager;

/** Eine Zutat innerhalb eines Rezepts: Name, Menge (für die Basis-Portionszahl) und Einheit. */
public record Zutat(String name, double menge, String einheit) {
}
