// Creating the states for tree
public class Tree {

    private TreeState state;

    public Tree() {
        state = TreeState.GREEN;
    }

    public void burn() {
        if (isGreen()) {
            state = TreeState.BURNING;
        }
    }

    public void extinguish() {
        if (isBurning()) {
            state = TreeState.BURNED;
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