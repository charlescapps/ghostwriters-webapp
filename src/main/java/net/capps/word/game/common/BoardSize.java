package net.capps.word.game.common;

/**
 * Created by charlescapps on 1/15/15.
 */
public enum BoardSize {
    TALL(5, 5, 5, 4, 2, 2, "net/capps/word/layouts/DefaultTallSquareConfig.txt"),
    GRANDE(9, 8, 13, 8, 4, 4, "net/capps/word/layouts/DefaultGrandeSquareConfig.txt"),
    VENTI(13, 10, 16, 14, 7, 8, "net/capps/word/layouts/DefaultVentiSquareConfig.txt");

    private final int N; // Number of rows/cols in an NxN board
    private final int maxInitialWordSize; // Max size for words in the randomly generated starting game
    private final int x2; // Number of double-letter bonuses on the board
    private final int x3; // Number of triple-letter bonuses
    private final int x4; // Number of double-word bonuses
    private final int numMines; // Number of triple-word bonuses
    private final String defaultLayoutFile;

    private BoardSize(int N, int maxInitialWordSize, int x2, int x3, int x4, int numMines, String defaultLayoutFile) {
        this.N = N;
        this.maxInitialWordSize = maxInitialWordSize;
        this.x2 = x2;
        this.x3 = x3;
        this.x4 = x4;
        this.numMines = numMines;
        this.defaultLayoutFile = defaultLayoutFile;
    }

    public int getN() {
        return N;
    }

    public int getMaxInitialWordSize() {
        return maxInitialWordSize;
    }

    public int getX2() {
        return x2;
    }

    public int getX3() {
        return x3;
    }

    public int getX4() {
        return x4;
    }

    public int getNumMines() {
        return numMines;
    }

    public String getDefaultLayoutFile() {
        return defaultLayoutFile;
    }
}
