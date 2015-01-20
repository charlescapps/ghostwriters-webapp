package net.capps.word.game.common;

/**
 * Created by charlescapps on 1/15/15.
 */
public enum BoardSize {
    TALL(9, 7, "net/capps/word/layouts/DefaultTallSquareConfig.txt"),
    GRANDE(13, 9, "net/capps/word/layouts/DefaultGrandeSquareConfig.txt"),
    VENTI(15, 11, "net/capps/word/layouts/DefaultVentiSquareConfig.txt");

    private final int numRows;
    private final int maxInitialWordSize;
    private final String defaultLayoutFile;

    private BoardSize(int numRows, int maxInitialWordSize, String defaultLayoutFile) {
        this.numRows = numRows;
        this.maxInitialWordSize = maxInitialWordSize;
        this.defaultLayoutFile = defaultLayoutFile;
    }

    public int getNumRows() {
        return numRows;
    }

    public int getMaxInitialWordSize() {
        return maxInitialWordSize;
    }

    public String getDefaultLayoutFile() {
        return defaultLayoutFile;
    }
}
