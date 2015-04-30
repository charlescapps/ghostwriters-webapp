package net.capps.word.game.ranking;

import net.capps.word.game.common.BoardSize;
import net.capps.word.game.common.GameResult;

/**
 * Created by charlescapps on 3/29/15.
 */
public class EloRankingComputer {
    public static final int INITIAL_USER_RATING = 1000; // 1000 rating
    private static final EloRankingComputer INSTANCE = new EloRankingComputer();

    public static EloRankingComputer getInstance() {
        return INSTANCE;
    }

    public static int getInitialUserRating() {
        return INITIAL_USER_RATING;
    }

    private EloRankingComputer() { } // Singleton pattern

    public int computeRatingChangeForPlayerA(int dbRatingA, int dbRatingB, GameResult result, BoardSize boardSize) {
        final double ratingA = (double) dbRatingA;
        final double ratingB = (double) dbRatingB;
        final double actualScoreA = computeActualScore(result);
        final double expectedScoreA = computeExpectedScore(ratingA, ratingB);
        double ratingChangeA = boardSize.getRatingK() * (actualScoreA - expectedScoreA);
        return (int) Math.round(ratingChangeA);
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
