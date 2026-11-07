import java.util.function.Supplier;

/**
 * Compares three ways of computing the on-screen statistics on a large forest:
 * multi-pass (three streams), one pass (custom collector) and parallel.
 *
 * Usage: java StreamBenchmark [size]   (default 1500)
 */
public class StreamBenchmark {

    public static void main(String[] args) throws InvalidGridSizeException {
        int size = 1500;
        if (args.length > 0) {
            try {
                size = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Size must be a whole number, using " + size);
            }
        }

        Forest forest = new Forest(size, size);
        // Burn some trees so there is more than one state to count.
        forest.cells()
                .filter(cell -> (cell.row() + cell.col()) % 7 == 0)
                .forEach(cell -> cell.value().burn());

        System.out.println("Forest: " + size + " x " + size + " = " + (long) size * size
                + " trees, " + Runtime.getRuntime().availableProcessors() + " processor(s)");

        // Warm up the JIT.
        for (int i = 0; i < 3; i++) {
            multiPass(forest);
            forest.stats();
            forest.statsParallel();
        }

        report("multi-pass (3 streams)", () -> multiPass(forest));
        report("one pass, sequential  ", forest::stats);
        report("one pass, parallel    ", forest::statsParallel);

        System.out.println("All three agree: "
                + (multiPass(forest).equals(forest.stats())
                   && forest.stats().equals(forest.statsParallel())));
    }

    /** The old way: one traversal per statistic. */
    private static ForestStats multiPass(Forest forest) {
        var byState = forest.countBy(Tree::getState);
        var fuel = forest.countBy(Tree::isGreen, Tree::getSpecies);
        long fires = forest.cells().mapToLong(cell -> cell.value().getTimesBurned()).sum();
        return ForestStats.of(byState, fuel, fires);
    }

    private static void report(String label, Supplier<ForestStats> task) {
        int runs = 5;
        long start = System.nanoTime();
        for (int i = 0; i < runs; i++) {
            task.get();
        }
        long avgMs = (System.nanoTime() - start) / runs / 1_000_000;
        System.out.println(label + ": " + avgMs + " ms");
    }
}
