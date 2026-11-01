import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.Timer;

public class ForestSimulation implements KeyListener {

    private static final Logger LOGGER = Logger.getLogger(ForestSimulation.class.getName());
    private static final int DEFAULT_SIZE = 6;

    private Forest forest;
    private Fire fire;
    private Water water;
    private Wind wind;

    // Two interchangeable spread strategies; 'W' swaps between them while
    // the simulation is running.
    private SpreadRule calmRule;
    private SpreadRule windyRule;
    private boolean windEnabled = true;

    private ForestPanel panel;
    private StatsPanel stats;
    private JFrame frame;
    private Timer fireTimer;
    private Timer waterTimer;
    private Timer regrowthTimer;
    private Timer windTimer;
    private Timer lightningTimer;

    // How often the fire advances, in milliseconds. Slower than a redraw
    // tick so the spread is watchable rather than instant.
    private static final int FIRE_STEP_MS = 2000;

    private static final int WATER_RECHARGE_MS = 2000; // 1 second
    private static final double BASE_SPREAD_CHANCE = 0.25;
    private static final int WIND_SHIFT_MS = 8000;
    private static final int LIGHTNING_MS = 10000;

    private Map<Tree, Long> regrowingTrees = new HashMap<>();
    public static final int REGROWTH_CHECK_MS = 100;
    
    public ForestSimulation() {
        this(DEFAULT_SIZE, DEFAULT_SIZE);
    }

    public ForestSimulation(int rows, int columns) {
        // forest
        forest = createForest(rows, columns);
        // objects
        wind = new Wind();
        calmRule = SpreadRule.constant(BASE_SPREAD_CHANCE).scaledBy(SpreadRule.BY_SPECIES);
        windyRule = calmRule.scaledBy(wind);
        fire = new Fire(windyRule);
        water = new Water(0, 0);

        // GUI
        panel = new ForestPanel(forest, water);
        stats = new StatsPanel(forest, this::windStatus);
        frame = new JFrame("Forest Simulation");

        frame.add(stats, BorderLayout.NORTH);
        frame.add(panel, BorderLayout.CENTER);
        frame.pack();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.addKeyListener(this);
        frame.setFocusable(true);
        frame.setVisible(true);

        // Observer: when a tree burns out by itself, schedule it to regrow.
        // (Water-extinguished trees are scheduled directly in useWater.)
        forest.addListener(event -> {
            if (event.type() == ForestEvent.Type.BURNED_OUT) {
                scheduleRegrowth(event.tree());
            }
        });

        // Advance the fire on a timer rather than only when a key is
        // pressed, so it spreads on its own while the player is moving
        // water around.
        fireTimer = new Timer(FIRE_STEP_MS, e -> {
            fire.spread(forest);
            panel.repaint();
        });
        fireTimer.start();
        startRegrowthTimer();
        startRandomFire();

        // The wind changes direction every so often...
        windTimer = new Timer(WIND_SHIFT_MS, e -> {
            wind.shiftRandomly();
            stats.refresh();
        });
        windTimer.start();

        // ...and lightning keeps starting new fires, so the forest never
        // settles: it burns, regrows and burns again.
        lightningTimer = new Timer(LIGHTNING_MS, e -> startRandomFire());
        lightningTimer.start();

        forest.printTreeSpecies(); // Print the species of each tree in the forest

    }

    /**
     * Builds the forest, recovering from a bad size by falling back to the
     * default one so the simulation still starts.
     */
    private static Forest createForest(int rows, int columns) {
        try {
            return new Forest(rows, columns);
        } catch (InvalidGridSizeException e) {
            LOGGER.warning(e.getMessage() + " - using " + DEFAULT_SIZE + " x "
                    + DEFAULT_SIZE + " instead");
            try {
                return new Forest(DEFAULT_SIZE, DEFAULT_SIZE);
            } catch (InvalidGridSizeException impossible) {
                // The default is a compile-time constant that is valid, so
                // reaching here means the code itself is broken.
                throw new IllegalStateException("Default forest size is invalid", impossible);
            }
        }
    }

    private void startRegrowthTimer(){
        regrowthTimer = new Timer(REGROWTH_CHECK_MS, e ->{
            long currentTime = System.nanoTime();

                regrowingTrees.entrySet().removeIf(entry -> {
                    Tree tree = entry.getKey();
                    long regrowthDeadLine = entry.getValue();

                    if(currentTime >= regrowthDeadLine){
                        if (tree.isBurned()){
                            tree.regrow();
                            forest.reportRegrown(tree);
                        }
                        return true; 
                    }
                    return false;
                });
                panel.repaint();
            });
            regrowthTimer.start();
    }

    /** Lightning: ignites one randomly chosen green tree (if any are left). */
    public void startRandomFire() {
        List<GridCell<Tree>> greenTrees = forest.cells()
                .filter(cell -> cell.value().isGreen())
                .toList();

        if (greenTrees.isEmpty()) {
            return; // nothing left to burn
        }

        GridCell<Tree> target = greenTrees.get(
                ThreadLocalRandom.current().nextInt(greenTrees.size()));
        fire.startFire(target.value());
        forest.reportBurning(target.value());
        LOGGER.info("Lightning struck (" + target.row() + ", " + target.col() + ")");
    }

    private String windStatus() {
        return windEnabled ? wind.describe() : "Wind: off (press W)";
    }

    /** Queues a burned tree to grow back after its species' regrowth time. */
    private void scheduleRegrowth(Tree tree) {
        long regrowthTimeNs = tree.getRegrowthTime() * 1_000_000_000L; // seconds to nanoseconds
        regrowingTrees.put(tree, System.nanoTime() + regrowthTimeNs);
    }

    // WATER
    public void useWater() {
        try {
            Tree tree = forest.getTree(
                    water.getRow(),
                    water.getCol());

            if (water.extinguish(tree)) {
                forest.reportExtinguished(tree);

                scheduleRegrowth(tree);

                startWaterRecharge();
            }

        } catch (InvalidGridPositionException e) {
            System.out.println("Could not use water: " + e.getMessage());
        }

        panel.repaint();
    }

    public void moveWater(String direction) {
        if (direction.equals("UP")) {
            water.moveUp();
        } else if (direction.equals("DOWN")) {
            water.moveDown(forest.getRows());
        } else if (direction.equals("LEFT")) {
            water.moveLeft();
        } else if (direction.equals("RIGHT")) {
            water.moveRight(forest.getColumns());
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            moveWater("UP");
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            moveWater("DOWN");
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            moveWater("LEFT");
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            moveWater("RIGHT");
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            useWater();
        } else if (e.getKeyCode() == KeyEvent.VK_W) {
            toggleWind();
        }

        panel.repaint();
    }

    /** Swaps the fire's spread strategy at runtime. */
    private void toggleWind() {
        windEnabled = !windEnabled;
        fire.setRule(windEnabled ? windyRule : calmRule);
        stats.refresh();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Not used
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }

    private void startWaterRecharge() {
        if (waterTimer != null && waterTimer.isRunning()) {
            return;
        }

        final long startTime = System.nanoTime();

        waterTimer = new Timer(100, e -> {

            long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;

            int progress = (int) Math.min(
                    100,
                    elapsedMs * 100 / WATER_RECHARGE_MS);

            water.updateRecharge(progress);
            panel.repaint();

            if (progress >= 100) {
                waterTimer.stop();
            }
        });

        waterTimer.start();

    }




    /**
     * Optional arguments: [rows] [columns], e.g. "java ForestSimulation 8 10".
     * Anything unusable falls back to the default size.
     */
    public static void main(String[] args) {
        int rows = DEFAULT_SIZE;
        int columns = DEFAULT_SIZE;
        try {
            if (args.length >= 2) {
                rows = Integer.parseInt(args[0]);
                columns = Integer.parseInt(args[1]);
            }
        } catch (NumberFormatException e) {
            LOGGER.warning("Grid size arguments must be whole numbers ("
                    + e.getMessage() + ") - using the default size");
            rows = DEFAULT_SIZE;
            columns = DEFAULT_SIZE;
        }
        new ForestSimulation(rows, columns);
    }
}
