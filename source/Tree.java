// Represents a tree and controls its current state.
public class Tree {

    private TreeState state;
    private int burnTime;
    private int restoreTime;

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

    public int getBurnTime() {
        return burnTime;
    }

    public int getRestoreTime() {
        return restoreTime;
    }
}