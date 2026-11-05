import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Forest {

    // The species that can appear in a new forest.
    private static final List<Supplier<Tree>> SPECIES =
            List.of(Pine::new, Oak::new, Eucalyptus::new);

    private final Grid<Tree> trees; // grid of trees

    // Observers interested in what happens to the trees.
    private final List<ForestListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * @throws InvalidGridSizeException if rows or columns is less than 1
     */
    public Forest(int rows, int columns) throws InvalidGridSizeException {
        Random random = new Random();
        trees = new Grid<>(rows, columns,
                (row, col) -> SPECIES.get(random.nextInt(SPECIES.size())).get());
    }

    /**
     * Returns the tree at (row, col).
     */
    public Tree getTree(int row, int col) throws InvalidGridPositionException {
        return trees.get(row, col);
    }

    public int getRows() {
        return trees.getRows();
    }

    public int getColumns() {
        return trees.getColumns();
    }

    /** Every tree with its position, for stream pipelines. */
    public Stream<GridCell<Tree>> cells() {
        return trees.cells();
    }

    /** The trees directly above/below/left/right of a position. */
    public Stream<GridCell<Tree>> neighbours(int row, int col) {
        return trees.neighbours(row, col);
    }

    /**
     * Counts trees grouped by any property
     */
    public <K extends Comparable<? super K>> Map<K, Long> countBy(
            Function<? super Tree, ? extends K> classifier) {
        return countBy(tree -> true, classifier);
    }

    /** Same as countBy(classifier) but only counts trees matching the filter. */
    public <K extends Comparable<? super K>> Map<K, Long> countBy(
            Predicate<? super Tree> filter,
            Function<? super Tree, ? extends K> classifier) {
        return trees.cells()
                .map(GridCell::value)
                .filter(filter)
                .collect(Collectors.groupingBy(classifier, TreeMap::new,
                        Collectors.counting()));
    }

    /** Tells listeners that a tree has started burning. */
    public void reportBurning(Tree tree) {
        notifyListeners(ForestEvent.Type.IGNITED, tree);
    }

    /** Tells listeners that the player put a burning tree out. */
    public void reportExtinguished(Tree tree) {
        notifyListeners(ForestEvent.Type.EXTINGUISHED, tree);
    }

    /** Records that a tree finished burning without help from the player. */
    public void reportBurnedOut(Tree tree) {
        notifyListeners(ForestEvent.Type.BURNED_OUT, tree);
    }

    /** Records that a burned tree has grown back. */
    public void reportRegrown(Tree tree) {
        notifyListeners(ForestEvent.Type.REGROWN, tree);
    }

    public void addListener(ForestListener listener) {
        listeners.add(listener);
    }

    /** Observers should call this when they go away, or the Forest keeps them alive. */
    public void removeListener(ForestListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(ForestEvent.Type type, Tree tree) {
        ForestEvent event = new ForestEvent(type, tree);
        listeners.forEach(listener -> listener.onForestEvent(event));
    }

    /**
     * Every tree that is on fire right now. Worked out from the trees
     * themselves each time, so there is only one source of truth for whether
     * a tree is burning (previously a separate list had to be kept in sync).
     */
    public List<Tree> getBurningTrees() {
        return trees.cells()
                .map(GridCell::value)
                .filter(Tree::isBurning)
                .toList();
    }

    //temporary method
    public void printTreeSpecies() {
        trees.cells()
                .collect(Collectors.groupingBy(GridCell::row, TreeMap::new,
                        Collectors.mapping(c -> c.value().getSpecies(),
                                Collectors.joining(" "))))
                .values()
                .forEach(System.out::println);
    }
}
