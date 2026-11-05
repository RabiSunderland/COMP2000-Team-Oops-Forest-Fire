/**
 * One position in a {@link Grid} together with the value stored there.
 *
 * @param <T> the type of value held in the grid
 */
public record GridCell<T>(int row, int col, T value) {
}
