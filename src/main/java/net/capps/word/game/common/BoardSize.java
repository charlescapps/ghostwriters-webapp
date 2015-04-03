package net.capps.word.game.common;

/**
 * Created by charlescapps on 1/15/15.
 */
public enum BoardSize {
    TALL(5, 5, 5, 4, 2, 1, "net/capps/word/layouts/DefaultTallSquareConfig.txt", 24.d),
    GRANDE(9, 8, 13, 8, 4, 4, "net/capps/word/layouts/DefaultGrandeSquareConfig.txt", 32.d),
    VENTI(13, 10, 16, 14, 7, 6, "net/capps/word/layouts/DefaultVentiSquareConfig.txt", 40.d);

    private final int N; // Number of rows/cols in an NxN board
    private final int maxInitialWordSize; // Max size for words in the randomly generated starting game
    private final int x2; // Number of double-letter bonuses on the board
    private final int x3; // Number of triple-letter bonuses
    private final int x4; // Number of quad-letter bonuses
    private final int x5; // Number of penta-letter bonuses
    private final String defaultLayoutFile;
    private final double ratingK;

    private BoardSize(int N, int maxInitialWordSize, int x2, int x3, int x4, int x5, String defaultLayoutFile, double ratingK) {
        this.N = N;
        this.maxInitialWordSize = maxInitialWordSize;
        this.x2 = x2;
        this.x3 = x3;
        this.x4 = x4;
        this.x5 = x5;
        this.defaultLayoutFile = defaultLayoutFile;
        this.ratingK = ratingK;
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

    public int getX5() {
        return x5;
    }

    public String getDefaultLayoutFile() {
        return defaultLayoutFile;
    }

    public double getRatingK() {
        return ratingK;
    }
}
