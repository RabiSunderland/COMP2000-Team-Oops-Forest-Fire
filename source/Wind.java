import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Wind is one concrete SpreadRule: fire spreads easily in the direction the
 * wind blows, and struggles against it. Because the direction changes over
 * time, the same forest produces very different fires.
 */
public class Wind implements SpreadRule {

    public enum Direction {
        CALM(0, 0), NORTH(-1, 0), EAST(0, 1), SOUTH(1, 0), WEST(0, -1);

        private final int dRow;
        private final int dCol;

        Direction(int dRow, int dCol) {
            this.dRow = dRow;
            this.dCol = dCol;
        }
    }

    private static final double DOWNWIND = 2.0;
    private static final double UPWIND = 0.3;

    private Direction direction;

    public Wind() {
        this.direction = Direction.CALM;
        shiftRandomly();
    }

    @Override
    public double chance(GridCell<Tree> from, GridCell<Tree> to) {
        // Dot product of the spread direction and the wind direction:
        // positive = with the wind, negative = against it, zero = sideways.
        int alignment = (to.row() - from.row()) * direction.dRow
                + (to.col() - from.col()) * direction.dCol;

        if (alignment > 0) {
            return DOWNWIND;
        }
        if (alignment < 0) {
            return UPWIND;
        }
        return 1.0; // calm, or fire spreading sideways to the wind
    }

    /** Switches to a different direction (possibly calm) chosen at random. */
    public void shiftRandomly() {
        Direction[] options = Arrays.stream(Direction.values())
                .filter(d -> d != direction)
                .toArray(Direction[]::new);
        direction = options[ThreadLocalRandom.current().nextInt(options.length)];
    }

    public Direction getDirection() {
        return direction;
    }

    public String describe() {
        return direction == Direction.CALM ? "Wind: calm" : "Wind: " + direction;
    }
}
