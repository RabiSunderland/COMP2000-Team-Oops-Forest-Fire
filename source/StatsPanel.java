import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A second observer of the Forest: shows live counts of what is happening.
 */
public class StatsPanel extends JPanel {

    private final Forest forest;
    private final Supplier<String> extraStatus; // e.g. the wind, supplied as a lambda
    private final JLabel label = new JLabel();

    public StatsPanel(Forest forest, Supplier<String> extraStatus) {
        this.forest = forest;
        this.extraStatus = extraStatus;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        setPreferredSize(new Dimension(forest.getColumns() * 80, 50));
        add(label, BorderLayout.CENTER);

        forest.addListener(event -> refresh());
        refresh();
    }

    public void refresh() {
        Map<TreeState, Long> states = forest.countBy(Tree::getState);

        // Green trees by species = how much fuel of each kind is left.
        String fuel = forest.countBy(Tree::isGreen, Tree::getSpecies)
                .entrySet().stream()
                .map(e -> e.getKey() + " " + e.getValue())
                .collect(Collectors.joining("  "));
				
		// Count the total number of times trees have caught fire.
		long totalFires = forest.cells()
				.mapToLong(cell -> cell.value().getTimesBurned())
				.sum();
				
        String extra = extraStatus.get();
        label.setText("<html>Green " + states.getOrDefault(TreeState.GREEN, 0L)
                + "  |  Burning " + states.getOrDefault(TreeState.BURNING, 0L)
                + "  |  Burned " + states.getOrDefault(TreeState.BURNED, 0L)
				+ "  |  Total Fires " + totalFires
                + (extra.isEmpty() ? "" : "  |  " + extra)
                + "<br>Fuel left: " + fuel + "</html>");
    }
}
