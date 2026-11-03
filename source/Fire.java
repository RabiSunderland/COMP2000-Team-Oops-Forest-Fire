import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Fire {

    private static final int BURN_DURATION_TICKS = 10;
    private static final double SPREAD_CHANCE = 0.10;

    // up, down, left, right
    private static final int[][] NEIGHBOUR_OFFSETS = { {-1, 0}, {1, 0}, {0, -1}, {0, 1} };

    private final Map<Tree, Integer> burnTimers = new HashMap<>();
    private final Random random = new Random();

    // Decides if a green neighbour catches fire. Passed in as behaviour so
    // other rules (e.g. wind) can be plugged in without touching spread().
    private Predicate<Tree> spreadRule = tree -> random.nextDouble() < SPREAD_CHANCE;

    public void setSpreadRule(Predicate<Tree> spreadRule) {
        this.spreadRule = spreadRule;
    }

    public void startFire(Tree tree) {
        if (tree.getState() == TreeState.GREEN) {
            tree.burn();
            burnTimers.put(tree, BURN_DURATION_TICKS);
        }
    }

    public void spread(Forest forest) {
        List<Cell> burningCells = cells(forest)
                .filter(cell -> treeAt(forest, cell)
                        .map(tree -> tree.getState() == TreeState.BURNING)
                        .orElse(false))
                .collect(Collectors.toList());

        burningCells.forEach(cell ->
                treeAt(forest, cell).ifPresent(tree -> burnTimers.putIfAbsent(tree, BURN_DURATION_TICKS)));

        // Every burning tree gets its own chance on each neighbour, then
        // all new fires start together so the order of the scan doesn't matter
        List<Tree> newlyIgnited = burningCells.stream()
                .flatMap(cell -> neighboursOf(forest, cell))
                .filter(tree -> tree.getState() == TreeState.GREEN)
                .filter(spreadRule)
                .distinct()
                .collect(Collectors.toList());

        newlyIgnited.forEach(tree -> {
            tree.burn();
            burnTimers.put(tree, BURN_DURATION_TICKS);
            forest.reportBurning(tree);
        });

        burnTimers.replaceAll((tree, ticksLeft) -> ticksLeft - 1);

        burnTimers.entrySet().stream()
                .filter(entry -> entry.getValue() <= 0)
                .map(Map.Entry::getKey)
                .filter(tree -> tree.getState() == TreeState.BURNING)
                .forEach(tree -> {
                    tree.extinguish();
                    forest.reportExtinguished(tree);
                });

        // Burned out, or put out by the water
        burnTimers.keySet().removeIf(tree -> tree.getState() != TreeState.BURNING);
    }

    private Stream<Cell> cells(Forest forest) {
        return IntStream.range(0, forest.getRows())
                .boxed()
                .flatMap(row -> IntStream.range(0, forest.getColumns())
                        .mapToObj(col -> new Cell(row, col)));
    }

    private Stream<Tree> neighboursOf(Forest forest, Cell cell) {
        return Stream.of(NEIGHBOUR_OFFSETS)
                .map(offset -> new Cell(cell.row + offset[0], cell.col + offset[1]))
                .map(neighbour -> treeAt(forest, neighbour))
                .flatMap(Optional::stream);
    }

    // getTree throws a checked exception, which can't be thrown from inside a
    // lambda, so off-grid positions become an empty Optional instead
    private Optional<Tree> treeAt(Forest forest, Cell cell) {
        try {
            return Optional.of(forest.getTree(cell.row, cell.col));
        } catch (InvalidGridPositionException e) {
            return Optional.empty();
        }
    }

    private static class Cell {
        private final int row;
        private final int col;

        Cell(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }
}
