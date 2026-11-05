/**
 * Observer interface for the Forest. It has a single abstract
 * method so listeners can be written as lambdas:
 *
 *     forest.addListener(event -> repaint());
 */
@FunctionalInterface
public interface ForestListener {
    void onForestEvent(ForestEvent event);
}
