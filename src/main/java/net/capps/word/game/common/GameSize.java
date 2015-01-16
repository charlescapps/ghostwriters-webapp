package net.capps.word.game.common;

/**
 * Created by charlescapps on 1/15/15.
 */
public enum GameSize {
    TALL(9), GRANDE(13), VENTI(15);

    private final int numRows;

    private GameSize(int numRows) {
        this.numRows = numRows;
    }

    public int getNumRows() {
        return numRows;
    }
}
