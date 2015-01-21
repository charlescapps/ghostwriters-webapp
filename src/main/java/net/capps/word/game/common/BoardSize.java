package net.capps.word.game.common;

/**
 * Created by charlescapps on 1/15/15.
 */
public enum BoardSize {
    TALL(9, 7, 13, 8, 4, 3, "net/capps/word/layouts/DefaultTallSquareConfig.txt"),
    GRANDE(13, 9, 17, 12, 7, 6, "net/capps/word/layouts/DefaultGrandeSquareConfig.txt"),
    VENTI(15, 11, 19, 14, 9, 8, "net/capps/word/layouts/DefaultVentiSquareConfig.txt");

    private final int N; // Number of rows/cols in an NxN board
    private final int maxInitialWordSize; // Max size for words in the randomly generated starting game
    private final int dl; // Number of double-letter bonuses on the board
    private final int tl; // Number of triple-letter bonuses
    private final int dw; // Number of double-word bonuses
    private final int tw; // Number of triple-word bonuses
    private final String defaultLayoutFile;

    private BoardSize(int N, int maxInitialWordSize, int dl, int tl, int dw, int tw, String defaultLayoutFile) {
        this.N = N;
        this.maxInitialWordSize = maxInitialWordSize;
        this.dl = dl;
        this.tl = tl;
        this.dw = dw;
        this.tw = tw;
        this.defaultLayoutFile = defaultLayoutFile;
    }

    public int getN() {
        return N;
    }

    public int getMaxInitialWordSize() {
        return maxInitialWordSize;
    }

    public int getDl() {
        return dl;
    }

    public int getTl() {
        return tl;
    }

    public int getDw() {
        return dw;
    }

    public int getTw() {
        return tw;
    }

    public String getDefaultLayoutFile() {
        return defaultLayoutFile;
    }
}
