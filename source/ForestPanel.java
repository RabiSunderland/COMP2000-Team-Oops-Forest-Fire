import java.awt.*;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.*;

public class ForestPanel extends JPanel{
    private Forest forest;
    private Water water;

    private final int CELL_SIZE = 80; // Size of each cell in the grid

        // Normal state palette
    private static final Color[] TREE_GREEN = {
        Color.decode("#355432"), Color.decode("#64945f"), Color.decode("#1b3618")
    };
    private static final Color GROUND_GREEN = Color.decode("#306b2a");
    private static final Color GROUND_BROWN = Color.decode("#302016");

    // Burning / burned share the same ground palette
    private static final Color SCORCHED_DARK = Color.decode("#211f1d");
    private static final Color SCORCHED_ASH = Color.decode("#7a7978");
    private static final Color SCORCHED_EMBER = Color.decode("#911d10");

    private static final Color FIRE_COLOR = Color.decode("#911d10");
    private static final Color BURNED_TREE_COLOR = Color.decode("#211f1d");
    private static final Color WATER_COLOR = Color.decode("#1551a1");
    private static final Color STATUS_TEXT_COLOR = Color.decode("#1b3618");

    // One way of drawing a cell for each tree state
    @FunctionalInterface
    private interface CellPainter {
        void paint(Graphics2D g, int row, int col);
    }

    private final Map<TreeState, CellPainter> painters = new EnumMap<>(TreeState.class);

    public ForestPanel(Forest forest, Water water) {
        this.forest = forest;
        this.water = water;

        painters.put(TreeState.GREEN, this::paintGreen);
        painters.put(TreeState.BURNING, this::paintBurning);
        painters.put(TreeState.BURNED, this::paintBurned);

        setPreferredSize(new Dimension(
            forest.getColumns() * CELL_SIZE,
            forest.getRows() * CELL_SIZE + 40
        ));
    }

    @Override
    protected void paintComponent(Graphics g0) {
    super.paintComponent(g0);
    Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawForest(g);
        drawWater(g);
        drawStatus(g);
    }

    private void drawForest(Graphics2D g) {
        IntStream.range(0, forest.getRows()).forEach(row ->
            IntStream.range(0, forest.getColumns()).forEach(col ->
                treeAt(row, col).ifPresent(tree -> {
                    painters.get(tree.getState()).paint(g, row, col);

                    g.setColor(new Color(0, 0, 0, 40));
                    g.drawRect(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                })));
    }

    private void paintGreen(Graphics2D g, int row, int col) {
        // Ground: 80% green / 20% brown
        g.setColor(groundSeed(row, col) < 80 ? GROUND_GREEN : GROUND_BROWN);
        g.fillRect(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);

        // Tree: one of 3 greens, picked randomly per tree
        int treeSeed = (row * 13 + col * 7) % TREE_GREEN.length;
        drawCircle(g, row, col, CELL_SIZE / 2 - 14, TREE_GREEN[treeSeed]);
    }

    private void paintBurning(Graphics2D g, int row, int col) {
        drawScorchedGround(g, row, col);
        drawCircle(g, row, col, CELL_SIZE / 2 - 14, FIRE_COLOR);
    }

    private void paintBurned(Graphics2D g, int row, int col) {
        drawScorchedGround(g, row, col);
        drawCircle(g, row, col, CELL_SIZE / 5, BURNED_TREE_COLOR);
    }

    private void drawCircle(Graphics2D g, int row, int col, int r, Color color) {
        int cx = col * CELL_SIZE + CELL_SIZE / 2;
        int cy = row * CELL_SIZE + CELL_SIZE / 2;
        g.setColor(color);
        g.fillOval(cx - r, cy - r, r * 2, r * 2);
    }

    private int groundSeed(int row, int col) {
        return (row * 31 + col * 17) % 100;
    }

    // Shared by BURNING and BURNED: 10% dark / 80% ash / 10% ember
    private void drawScorchedGround(Graphics2D g, int row, int col) {
        int seed = groundSeed(row, col);
        if (seed < 10) {
            g.setColor(SCORCHED_DARK);
        } else if (seed < 90) {
            g.setColor(SCORCHED_ASH);
        } else {
            g.setColor(SCORCHED_EMBER);
        }
        g.fillRect(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
    }

    private void drawWater(Graphics g) {

        int x = water.getCol() * CELL_SIZE;
        int y = water.getRow() * CELL_SIZE;
        int cx = x + CELL_SIZE / 2;
        int cy = y + CELL_SIZE / 2;

        int diameter = (int) ((CELL_SIZE - 40) * (2.0 / 3.0));
        g.setColor(WATER_COLOR);
        g.fillOval(cx - diameter / 2, cy - diameter / 2, diameter, diameter);
    }

    // Live count of trees in each state, shown in the strip under the grid
    private void drawStatus(Graphics2D g) {
        Map<TreeState, Long> counts = IntStream.range(0, forest.getRows())
            .boxed()
            .flatMap(row -> IntStream.range(0, forest.getColumns())
                .mapToObj(col -> treeAt(row, col)))
            .flatMap(Optional::stream)
            .collect(Collectors.groupingBy(
                Tree::getState,
                () -> new EnumMap<>(TreeState.class),
                Collectors.counting()));

        String status = Arrays.stream(TreeState.values())
            .map(state -> state + ": " + counts.getOrDefault(state, 0L))
            .collect(Collectors.joining("     "));

        g.setColor(STATUS_TEXT_COLOR);
        g.drawString(status, 10, forest.getRows() * CELL_SIZE + 25);
    }

    private Optional<Tree> treeAt(int row, int col) {
        try {
            return Optional.of(forest.getTree(row, col));
        } catch (InvalidGridPositionException e) {
            System.out.println("Skipped drawing cell: " + e.getMessage());
            return Optional.empty();
        }
    }

}
