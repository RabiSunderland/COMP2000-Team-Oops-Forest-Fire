import java.util.ArrayList;
import java.util.List;

// represents a tree and controls its current state.
public class Tree {

    private TreeState state;

    // keeps track of the tree's previous states.
    private final List<TreeState> stateHistory = new ArrayList<>();

    public Tree() {
        state = TreeState.GREEN;
        stateHistory.add(state);
    }

    public void burn() {
        changeState(TreeState.BURNING);
    }

    public void extinguish() {
        changeState(TreeState.BURNED);
    }

    /** The tree has finished burning by itself (as opposed to being put out). */
    public void burnOut() {
        changeState(TreeState.BURNED);
    }

    public void regrow() {
        changeState(TreeState.GREEN);
    }

    public int getRegrowthTime() {
        return 20;
    }

    private void changeState(TreeState newState) {
        if (state.canTransitionTo(newState)) {
            state = newState;
            stateHistory.add(state);
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

    // returns a copy of tree state history meh.
    public List<TreeState> getStateHistory() {
        return new ArrayList<>(stateHistory);
    }

    // uses stream to count.
    public long getTimesBurned() {
        return stateHistory.stream()
                .filter(s -> s == TreeState.BURNING)
                .count();
    }

    public String getSpecies() {
        return "Tree";
    }

    public double getIgnitionMultiplier() {
        return 1.0;
    }
}