package net.capps.word.game.common;

/**
 * Created by charlescapps on 1/15/15.
 */
public enum GameSize {
    TALL(9, "net/capps/word/layouts/DefaultTallSquareConfig.txt"),
    GRANDE(13, "net/capps/word/layouts/DefaultGrandeSquareConfig.txt"),
    VENTI(15, "net/capps/word/layouts/DefaultVentiSquareConfig.txt");

    private final int numRows;
    private final String defaultLayoutFile;

    private GameSize(int numRows, String defaultLayoutFile) {
        this.numRows = numRows;
        this.defaultLayoutFile = defaultLayoutFile;
    }

    public int getNumRows() {
        return numRows;
    }

    public String getDefaultLayoutFile() {
        return defaultLayoutFile;
    }
}
