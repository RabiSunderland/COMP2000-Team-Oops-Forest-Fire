/**
 * The possible states a Tree can be in.
 */
public enum TreeState {
    GREEN,
    BURNING,
    BURNED;

    public boolean canTransitionTo(TreeState nextState) {
        return (this == GREEN && nextState == BURNING)
                || (this == BURNING && nextState == BURNED)
                || (this == BURNED && nextState == GREEN);
    }
}