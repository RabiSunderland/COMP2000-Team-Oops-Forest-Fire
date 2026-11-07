import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class Fire {

    private static final double BASE_SPREAD_CHANCE = 0.25;

    // Strategy: how likely the fire is to jump to a neighbour. Can be swapped
    // while the simulation is running (see setRule).
    private SpreadRule rule;

    // How long each currently burning tree has been burning, in fire steps.
    private final Map<Tree, Integer> burnAge = new HashMap<>();

    public Fire() {
        this(SpreadRule.constant(BASE_SPREAD_CHANCE).scaledBy(SpreadRule.BY_SPECIES));
    }

    public Fire(SpreadRule rule) {
        this.rule = Objects.requireNonNull(rule, "spread rule");
    }

    public void setRule(SpreadRule rule) {
        this.rule = Objects.requireNonNull(rule, "spread rule");
    }

    public void startFire(Tree tree) {

        if (tree != null && tree.isGreen()) {
            tree.burn();
        }
    }

    /**
     * Advances the fire by one step: burning trees try to ignite their green
     * neighbours, then every burning tree gets a step older and burns out
     * once it reaches its own burn duration.
     */
    public void spread(Forest forest) {
        // Snapshot first, so a tree that catches fire during this step does
        // not also get to spread fire during the same step.
        List<GridCell<Tree>> burning = forest.cells()
                .filter(cell -> cell.value().isBurning())
                .toList();

        burning.forEach(from -> forest.neighbours(from.row(), from.col())
                .filter(to -> to.value().isGreen())
                .filter(to -> ThreadLocalRandom.current().nextDouble() < rule.chance(from, to))
                .forEach(to -> ignite(forest, to.value())));

        // Forget trees that the player put out with water.
        burnAge.keySet().removeIf(tree -> !tree.isBurning());

        burning.forEach(cell -> age(forest, cell.value()));
    }

    private void ignite(Forest forest, Tree tree) {
        startFire(tree);
        forest.reportBurning(tree);
    }

    private void age(Forest forest, Tree tree) {
        int ticksBurning = burnAge.merge(tree, 1, Integer::sum);

        if (ticksBurning >= tree.getBurnDuration()) {
            tree.burnOut();
            burnAge.remove(tree);
            forest.reportBurnedOut(tree);
        }
    }
}
