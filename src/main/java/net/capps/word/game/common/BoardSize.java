package net.capps.word.game.common;

/**
 * Created by charlescapps on 1/15/15.
 */
public enum BoardSize {
    TALL(5, 5, 4, 2, 1, "net/capps/word/layouts/DefaultTallSquareConfig.txt", 50.d, 5, 1),
    GRANDE(9, 13, 8, 4, 4, "net/capps/word/layouts/DefaultGrandeSquareConfig.txt", 100.d, 10, 1),
    VENTI(13, 16, 14, 7, 6, "net/capps/word/layouts/DefaultVentiSquareConfig.txt", 200.d, 20, 2);

    private final int N; // Number of rows/cols in an NxN board
    private final int x2; // Number of double-letter bonuses on the board
    private final int x3; // Number of triple-letter bonuses
    private final int x4; // Number of quad-letter bonuses
    private final int x5; // Number of penta-letter bonuses
    private final String defaultLayoutFile;
    private final double ratingK;
    private final int minimumRatingIncrease;
    private final int tokenCost;

    private BoardSize(int N, int x2, int x3, int x4, int x5,
                      String defaultLayoutFile, double ratingK, int minimumRatingIncrease, int tokenCost) {
        this.N = N;
        this.x2 = x2;
        this.x3 = x3;
        this.x4 = x4;
        this.x5 = x5;
        this.defaultLayoutFile = defaultLayoutFile;
        this.ratingK = ratingK;
        this.minimumRatingIncrease = minimumRatingIncrease;
        this.tokenCost = tokenCost;
    }

    public int getN() {
        return N;
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

    public int getMinimumRatingIncrease() {
        return minimumRatingIncrease;
    }

    public int getTokenCost() {
        return tokenCost;
    }
}
