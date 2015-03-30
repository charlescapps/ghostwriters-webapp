package net.capps.word.game.ranking;

import net.capps.word.game.common.GameResult;

/**
 * Created by charlescapps on 3/29/15.
 */
public class EloRankingComputer {
    public static final int AVERAGE_RATING_DB = 1500000; // 1500 rating
    public static final double DATABASE_FACTOR = 1000.d;
    private static final EloRankingComputer INSTANCE = new EloRankingComputer();
    private static final double K = 32.d;

    public static EloRankingComputer getInstance() {
        return INSTANCE;
    }

    private EloRankingComputer() { } // Singleton pattern

    public int computeRatingChangeForPlayerA(int dbRatingA, int dbRatingB, GameResult result) {
        final double ratingA = (double) dbRatingA / DATABASE_FACTOR;
        final double ratingB = (double) dbRatingB / DATABASE_FACTOR;
        final double actualScoreA = computeActualScore(result);
        final double expectedScoreA = computeExpectedScore(ratingA, ratingB);
        double ratingChangeA = K * (actualScoreA - expectedScoreA);
        return (int) (ratingChangeA * DATABASE_FACTOR);
    }

    // ----------- Private -------------
    private static double computeActualScore(GameResult gameResult) {
        switch (gameResult) {
            case PLAYER1_WIN: return 1.0d;
            case PLAYER2_WIN: return 0.0d;
            case TIE: return 0.5d;
        }
        throw new IllegalArgumentException("Can only compute ratings for games that are PLAYER1_WIN, PLAYER2_WIN or TIE");
    }
    private static double computeExpectedScore(double ratingA, double ratingB) {
        double q_a = computeQ(ratingA);
        double q_b = computeQ(ratingB);
        return q_a / (q_a + q_b);
    }

    private static double computeQ(final double rating) {
        final double exp = rating / 400.d;
        return Math.pow(10.d, exp);
    }

}
