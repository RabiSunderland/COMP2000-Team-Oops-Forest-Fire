/**
 * The possible states a Tree can be in.
 */
public enum TreeState {
    GREEN,
    BURNING,
    BURNED;

    // Returns the next state in the tree lifecycle.
    public TreeState nextState() {
        switch (this) {
            case GREEN:
                return BURNING;
            case BURNING:
                return BURNED;
            case BURNED:
                return GREEN;
            default:
                throw new IllegalStateException("Unknown tree state");
        }
    }

    // Checks whether a state transition is valid.
    public boolean canTransitionTo(TreeState targetState) {
        return nextState() == targetState;
    }
}