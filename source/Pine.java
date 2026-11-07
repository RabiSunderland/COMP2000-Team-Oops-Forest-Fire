public class Pine extends Tree {
    @Override
    public String getSpecies() {
        return "Pine";
    }

    @Override
    public double getIgnitionMultiplier() {
        return 1.2; // Pine trees are more flammable than generic trees
    }

    @Override
    public int getBurnDuration() {
        return 3;
    }

    @Override
    public int getRegrowthTime() {
        return 15; // Pine trees regrow faster than generic trees
    }
    
}
