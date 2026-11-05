/**
 * Strategy interface: decides how likely fire is to jump from one cell to a
 * neighbouring one. Fire holds a SpreadRule and asks it, it never knows which
 * concrete rule it has been given.
 *
 * It is a functional interface, so simple rules are lambdas and only rules
 * with their own state (like Wind) need a class. Rules can be combined with
 * scaledBy(), which is why the result is a multiplier-style probability.
 */
@FunctionalInterface
public interface SpreadRule {

    /** Probability (0.0 - 1.0) that fire spreads from 'from' to 'to' this tick. */
    double chance(GridCell<Tree> from, GridCell<Tree> to);

    /** A rule that ignores the cells and always returns the same chance. */
    static SpreadRule constant(double probability) {
        return (from, to) -> probability;
    }

    /** Species matter: eucalyptus catches more easily than oak. */
    SpreadRule BY_SPECIES = (from, to) -> to.value().getIgnitionMultiplier();

    /** Combines two rules by multiplying their chances (capped at 1.0). */
    default SpreadRule scaledBy(SpreadRule other) {
        return (from, to) -> Math.min(1.0, chance(from, to) * other.chance(from, to));
    }
}
