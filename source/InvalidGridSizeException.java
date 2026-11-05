/**
 * Thrown when a Grid (and therefore a Forest) is asked to be created with a
 * size that cannot exist, e.g. zero or negative rows/columns.
 *
 * This is a checked exception on purpose: the size can come from outside the
 * program (command line arguments), so callers must decide how to recover
 * instead of letting the program crash later with an obscure error.
 */
public class InvalidGridSizeException extends Exception {

    private final int rows;
    private final int columns;

    public InvalidGridSizeException(int rows, int columns) {
        super("Invalid grid size: " + rows + " x " + columns
                + " (both dimensions must be at least 1)");
        this.rows = rows;
        this.columns = columns;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }
}
