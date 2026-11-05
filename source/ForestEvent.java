/**
 * Something that happened to a tree in the forest. Sent to every
 * {@link ForestListener} registered with the Forest (Observer pattern).
 */
public record ForestEvent(Type type, Tree tree) {

    public enum Type {
        IGNITED,      // a green tree caught fire
        EXTINGUISHED, // the player put a burning tree out with water
        BURNED_OUT,   // a tree finished burning on its own
        REGROWN       // a burned tree grew back
    }
}
