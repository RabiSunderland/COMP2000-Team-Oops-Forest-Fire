public class Oak extends Tree {
    @Override
    public String getSpecies() {
        return "Oak";
    }

    @Override
    public double getIgnitionMultiplier() {
        return 0.6; // Oak trees are less flammable than generic trees
    }

    @Override
    public int getBurnDuration() {
        return 4; // dense wood burns slowly
    }

    @Override
    public int getRegrowthTime() {
        return 25; // Oak trees take longer to regrow than generic trees
    }
}
