// Represents a tree and controls its current state.
public class Tree implements Extinguishable {

    private TreeState state;

    public Tree() {
        state = TreeState.GREEN;
    }

    public void burn() {
        changeState(TreeState.BURNING);
    }

    public void extinguish() {
        changeState(TreeState.BURNED);
    }

    public void regrow() {
        changeState(TreeState.GREEN);
    }

    private void changeState(TreeState newState) {
        if (state.canTransitionTo(newState)) {
            state = newState;
        }
    }

    public boolean isGreen() {
        return state == TreeState.GREEN;
    }

    public boolean isBurning() {
        return state == TreeState.BURNING;
    }

    public boolean isBurned() {
        return state == TreeState.BURNED;
    }

    public TreeState getState() {
        return state;
    }
}