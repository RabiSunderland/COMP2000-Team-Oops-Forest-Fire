import java.awt.BorderLayout;
import java.awt.Dimension;
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
        setPreferredSize(new Dimension(forest.getColumns() * 80, 70));
        add(label, BorderLayout.CENTER);

        forest.addListener(event -> refresh());
        refresh();
    }

    public void refresh() {
        // One pass over the forest (custom collector) instead of three.
        ForestStats stats = forest.stats();

        String fuel = stats.getFuelBySpecies().entrySet().stream()
                .map(e -> e.getKey() + " " + e.getValue())
                .collect(Collectors.joining("  "));

        String extra = extraStatus.get();
        label.setText("<html>"
        + "🌳 Green: " + stats.count(TreeState.GREEN)
        + "  |  🔥 Burning: " + stats.count(TreeState.BURNING)
        + "  |  ⚫ Burned: " + stats.count(TreeState.BURNED)
        + "  |  📊 Total Fires: " + stats.getTotalFires()
        + "<br>" + (extra.isEmpty() ? "" : "&#8776; " + extra + "  |  ")
        + "🌲 Fuel: " + fuel
        + "</html>");
    }
}
