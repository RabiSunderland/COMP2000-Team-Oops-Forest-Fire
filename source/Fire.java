import java.util.ArrayList;
import java.util.List;

public class Fire {

    // Four-directional neighbours: up, down, left, right.
    private static final int[][] NEIGHBOUR_OFFSETS = {
        {-1, 0}, {1, 0}, {0, -1}, {0, 1}
    };

    // Chance that a given green neighbour (tree) catches fire on any one tick.
    private static final double SPREAD_CHANCE = 0.25;

    public void startFire(Tree tree) {

        if (tree.getState() == TreeState.GREEN) {
            tree.burn();
        }
    }

    /**
     * Advances the fire by one step: scans the whole grid for burning
     * trees, ignites any directly adjacent (up/down/left/right) 
     * tree that is currently green.
     *
     * The new ignitions are collected in toIgnite first and only applied
     * after the full scan (rather than igniting trees as we find them).
     */
    public void spread(Forest forest) {
        List<Tree> toIgnite = new ArrayList<>();

        for (int row = 0; row < forest.getRows(); row++) {
            for (int col = 0; col < forest.getColumns(); col++) {
                try {
                    Tree tree = forest.getTree(row, col);
                    if (!tree.isBurning()) {
                        continue;
                    }

                    for (int[] offset : NEIGHBOUR_OFFSETS) {
                        int neighbourRow = row + offset[0];
                        int neighbourCol = col + offset[1];

                        try {
                            Tree neighbour = forest.getTree(neighbourRow, neighbourCol);
                            if (neighbour.isGreen()
                                    && !toIgnite.contains(neighbour)
                                    && Math.random() < SPREAD_CHANCE) {
                                toIgnite.add(neighbour);
                            }
                        } catch (InvalidGridPositionException e) {
                            // Neighbour is off the edge of the grid.
                        }
                    }
                } catch (InvalidGridPositionException e) {
                    // Should not happen: row/col come from the forest's own dimensions.
                }
            }
        }

        for (Tree tree : toIgnite) {
            tree.burn();
            forest.reportBurning(tree);
        }
    }

}