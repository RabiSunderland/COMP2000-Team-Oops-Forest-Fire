public class Eucalyptus extends Tree {
    @Override
    public String getSpecies() {
        return "Eucalyptus";
    }

    @Override
    public double getIgnitionMultiplier() {
        return 1.5; // Eucalyptus trees are more flammable than generic trees
    }

    @Override
    public int getBurnDuration() {
        return 2; // burns hot and fast
    }

    @Override
    public int getRegrowthTime() {
        return 10; // Eucalyptus trees regrow faster than generic trees
    }
}
