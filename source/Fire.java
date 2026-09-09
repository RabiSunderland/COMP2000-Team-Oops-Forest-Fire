public class Fire {

    public void startFire(Tree tree) {

        if (tree.getState() == TreeState.GREEN) {
            tree.burn();
        }
    }

    public void spread(Forest forest) {
        // Needs to be done
    }

}