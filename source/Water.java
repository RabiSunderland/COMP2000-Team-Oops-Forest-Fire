public class Water {
    //water is controled by the user to put out the fire
    private int row;
    private int col;

    public Water(int row, int col) {
        this.row = row;
        this.col = col;
    }

    //move the water to a new position
    public void moveUp() {
        if (row > 0) {
            row--;
        }
    }

    public void moveDown(int maxRows) {
        if (row < maxRows - 1) {
            row++;
        }
    }

    public void moveLeft() {
        if (col > 0) {
            col--;
        }
    }

    public void moveRight(int maxCols) {
        if (col < maxCols - 1) {
            col++;
        }
    }

    public void extinguish(Extinguishable target) {
        if (target.isBurning()) {
            target.extinguish();
        }
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

}
