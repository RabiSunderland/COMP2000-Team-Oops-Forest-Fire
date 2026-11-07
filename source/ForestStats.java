import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collector;

/** Statistics for the whole forest, gathered in one pass by a custom Collector. */
public final class ForestStats {

    private final Map<TreeState, Long> countByState;
    private final Map<String, Long> fuelBySpecies; // green trees only
    private final long totalFires;

    private ForestStats(Map<TreeState, Long> countByState,
                        Map<String, Long> fuelBySpecies, long totalFires) {
        this.countByState = Map.copyOf(countByState);
        this.fuelBySpecies = Map.copyOf(fuelBySpecies);
        this.totalFires = totalFires;
    }

    /** Used by the benchmark to build the multi-pass baseline. */
    public static ForestStats of(Map<TreeState, Long> countByState,
                                 Map<String, Long> fuelBySpecies, long totalFires) {
        return new ForestStats(countByState, fuelBySpecies, totalFires);
    }

    public long count(TreeState state) {
        return countByState.getOrDefault(state, 0L);
    }

    /** Green trees still standing, per species (the "fuel" left to burn). */
    public Map<String, Long> getFuelBySpecies() {
        return new TreeMap<>(fuelBySpecies);
    }

    /** How many times trees have caught fire, counting repeat burns. */
    public long getTotalFires() {
        return totalFires;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ForestStats that
                && countByState.equals(that.countByState)
                && fuelBySpecies.equals(that.fuelBySpecies)
                && totalFires == that.totalFires;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(countByState, fuelBySpecies, totalFires);
    }

    @Override
    public String toString() {
        return "ForestStats" + new EnumMap<>(countByState).toString()
                + " fuel=" + new TreeMap<>(fuelBySpecies) + " totalFires=" + totalFires;
    }

    /**
     * Collector that builds a ForestStats from trees. Each thread fills its own
     * Accumulator and the results are merged, so no locking is needed.
     */
    public static Collector<Tree, ?, ForestStats> collector() {
        return Collector.of(
                Accumulator::new,
                Accumulator::add,
                Accumulator::merge,
                Accumulator::build,
                Collector.Characteristics.UNORDERED);
    }

    private static final class Accumulator {
        private final Map<TreeState, Long> countByState = new EnumMap<>(TreeState.class);
        private final Map<String, Long> fuelBySpecies = new TreeMap<>();
        private long totalFires;

        void add(Tree tree) {
            countByState.merge(tree.getState(), 1L, Long::sum);
            if (tree.isGreen()) {
                fuelBySpecies.merge(tree.getSpecies(), 1L, Long::sum);
            }
            totalFires += tree.getTimesBurned();
        }

        Accumulator merge(Accumulator other) {
            other.countByState.forEach((state, n) -> countByState.merge(state, n, Long::sum));
            other.fuelBySpecies.forEach((species, n) -> fuelBySpecies.merge(species, n, Long::sum));
            totalFires += other.totalFires;
            return this;
        }

        ForestStats build() {
            return new ForestStats(countByState, fuelBySpecies, totalFires);
        }
    }
}
