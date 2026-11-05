import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A fixed-size, rectangular grid holding values of type T.
 *
 * Generics are used so the grid is type safe
 * @param <T> the type of value stored in each cell
 */
public class Grid<T> {

    private final List<T> cells;
    private final int rows;
    private final int columns;

    /**
     * @param initialiser called once per cell with (row, col) to create the
     *                    value stored there
     * @throws InvalidGridSizeException if rows or columns is less than 1
     */
    public Grid(int rows, int columns,
                BiFunction<Integer, Integer, ? extends T> initialiser)
            throws InvalidGridSizeException {
        if (rows < 1 || columns < 1) {
            throw new InvalidGridSizeException(rows, columns);
        }
        this.rows = rows;
        this.columns = columns;
        this.cells = new ArrayList<>(rows * columns);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                cells.add(initialiser.apply(row, col));
            }
        }
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public boolean contains(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < columns;
    }

    public T get(int row, int col) throws InvalidGridPositionException {
        if (!contains(row, col)) {
            throw new InvalidGridPositionException(row, col);
        }
        return cells.get(row * columns + col);
    }

    /**
     * Every cell in the grid as a stream. Built from an IntStream range so
     * the stream splits cheaply, which makes it safe and effective to call
     * .parallel() on as long as the pipeline only reads.
     */
    public Stream<GridCell<T>> cells() {
        return IntStream.range(0, cells.size())
                .mapToObj(i -> new GridCell<>(i / columns, i % columns, cells.get(i)));
    }

    /**
     * The (up to four) cells directly above, below, left and right of a
     * position. Cells that would fall off the edge are simply left out, so
     * callers never need their own bounds checks.
     */
    public Stream<GridCell<T>> neighbours(int row, int col) {
        return Stream.of(new int[]{-1, 0}, new int[]{1, 0},
                         new int[]{0, -1}, new int[]{0, 1})
                .filter(d -> contains(row + d[0], col + d[1]))
                .map(d -> new GridCell<>(row + d[0], col + d[1],
                        cells.get((row + d[0]) * columns + (col + d[1]))));
    }
}
