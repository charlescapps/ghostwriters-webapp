package net.capps.word.rest.providers;

import net.capps.word.db.dao.GamesDAO;
import net.capps.word.db.dao.UsersDAO;
import net.capps.word.game.common.BoardSize;
import net.capps.word.game.common.GameResult;
import net.capps.word.game.ranking.EloRankingComputer;
import net.capps.word.ranks.UserWithRanking;
import net.capps.word.ranks.UserWithRating;
import net.capps.word.ranks.UserRanks;
import net.capps.word.rest.models.GameModel;
import net.capps.word.rest.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.String.format;

/**
 * Created by charlescapps on 3/29/15.
 */
public class RatingsProvider {
    private static final Logger LOG = LoggerFactory.getLogger(RatingsProvider.class);
    private static final RatingsProvider INSTANCE = new RatingsProvider();
    private static final EloRankingComputer eloRankingComputer = EloRankingComputer.getInstance();
    private static final UsersDAO usersDAO = UsersDAO.getInstance();
    private static final GamesDAO gamesDAO = GamesDAO.getInstance();
    private static final int MAX_RATING_INCREASE = 1000;
    private static final int MIN_RATING_DECREASE = -10;

    public static RatingsProvider getInstance() {
        return INSTANCE;
    }

    private RatingsProvider() { }

    public void updatePlayerRatings(GameModel gameModel, Connection dbConn)
            throws SQLException {
        final GameResult gameResult = gameModel.getGameResult();
        final UserModel player1 = gameModel.getPlayer1Model();
        final UserModel player2 = gameModel.getPlayer2Model();
        final BoardSize boardSize = gameModel.getBoardSize();
        updateWinLossRecords(player1, player2, gameResult);

        // Results that can affect player rating
        switch (gameResult) {
            case PLAYER1_WIN:
            case PLAYER2_RESIGN:
            case PLAYER2_WIN:
            case PLAYER1_RESIGN:
            case TIE:

                final int player1InitialRating = player1.getRating();
                final int player2InitialRating = player2.getRating();

                // Compute the idealized elo rating change if we were using pure Chess ratings
                final int player1EloRatingChange = eloRankingComputer.computeRatingChangeForPlayerA(player1InitialRating, player2InitialRating, gameResult, boardSize);

                // Modify rating change to always be positive for human players, based on a minimum increase for the board size
                int player1ActualRatingChange = computePlayer1RatingChange(player1EloRatingChange, gameResult, boardSize, player1);
                int player2ActualRatingChange = computePlayer2RatingChange(-player1EloRatingChange, gameResult, boardSize, player2);

                gameModel.setPlayer1RatingIncrease(player1ActualRatingChange);
                gameModel.setPlayer2RatingIncrease(player2ActualRatingChange);

                final int player1NewRating = player1InitialRating + player1ActualRatingChange;
                final int player2NewRating = player2InitialRating + player2ActualRatingChange;

                // Updates in the database
                usersDAO.updateUserRating(dbConn, player1.getId(), player1NewRating, gameResult.getPlayer1RecordChange());
                usersDAO.updateUserRating(dbConn, player2.getId(), player2NewRating, gameResult.getPlayer2RecordChange());
                gamesDAO.updateGamePlayerRatingIncreases(dbConn, gameModel.getId(), player1ActualRatingChange, player2ActualRatingChange);

                // Updates in the data structures for ranking
                UserRanks.getInstance().updateRankedUser(new UserWithRating(player1.getId(), player1NewRating));
                UserRanks.getInstance().updateRankedUser(new UserWithRating(player2.getId(), player2NewRating));

                // Update models to be returned to the app
                player1.setRating(player1NewRating);
                player2.setRating(player2NewRating);
            default:
                // Do nothing if the game isn't over due to a win or a tie.
        }
    }

    private void updateWinLossRecords(UserModel player1, UserModel player2, GameResult gameResult) {
        switch (gameResult) {
            case PLAYER1_WIN:
            case PLAYER2_RESIGN:
                player1.setWins(player1.getWins() + 1);
                player2.setLosses(player2.getLosses() + 1);
                break;
            case PLAYER2_WIN:
            case PLAYER1_RESIGN:
                player2.setWins(player2.getWins() + 1);
                player1.setLosses(player1.getLosses() + 1);
                break;
            case TIE:
                player1.setTies(player1.getTies() + 1);
                player2.setTies(player2.getTies() + 1);
        }
    }

    private int computePlayer1RatingChange(int rawPlayer1RatingChange, GameResult gameResult, BoardSize boardSize, UserModel userModel) {
        // 0 rating change for resigning player.
        if (gameResult == GameResult.PLAYER1_RESIGN) {
            return 0;
        }

        // Use the raw ELO chess rating change for the AI
        if (userModel.isAI()) {
            return boundRatingChange(rawPlayer1RatingChange);
        }

        // Make people happy by not allowing negative rating changes
        int ratingChange = Math.max(boardSize.getMinimumRatingIncrease(), rawPlayer1RatingChange);
        return boundRatingChange(ratingChange);
    }

    private int computePlayer2RatingChange(int rawPlayer2RatingChange, GameResult gameResult, BoardSize boardSize, UserModel userModel) {
        // 0 rating change for resigning player.
        if (gameResult == GameResult.PLAYER2_RESIGN) {
            return 0;
        }

        // Use the raw ELO chess rating change for the AI
        if (userModel.isAI()) {
            return boundRatingChange(rawPlayer2RatingChange);
        }

        // Make people happy by not allowing negative rating changes
        int ratingChange = Math.max(boardSize.getMinimumRatingIncrease(), rawPlayer2RatingChange);
        return boundRatingChange(ratingChange);
    }

    private int boundRatingChange(int ratingChange) {
        ratingChange = Math.max(ratingChange, MIN_RATING_DECREASE);
        ratingChange = Math.min(ratingChange, MAX_RATING_INCREASE);
        return ratingChange;
    }

    public List<UserModel> getUsersWithRatingsAroundMe(Connection dbConn, UserModel user, int count) throws SQLException {
        List<UserModel> ratingGEQ = usersDAO.getUsersWithRatingGEQ(dbConn, user.getId(), user.getRating(), count);
        List<UserModel> ratingLT = usersDAO.getUsersWithRatingLT(dbConn, user.getRating(), count);
        List<UserModel> resultUsers = new ArrayList<>(ratingGEQ.size() + ratingLT.size() + 1);
        resultUsers.addAll(ratingLT);
        resultUsers.add(user);
        resultUsers.addAll(ratingGEQ);
        Collections.reverse(resultUsers);
        return resultUsers;
    }

    public UserModel getBestMatch(Connection dbConn, UserModel user) throws SQLException {
        final int numAdjacentUsers = 50;
        List<UserModel> ratingGEQ = usersDAO.getUsersWithRatingGEQ(dbConn, user.getId(), user.getRating(), numAdjacentUsers);
        List<UserModel> ratingLT = usersDAO.getUsersWithRatingLT(dbConn, user.getRating(), numAdjacentUsers);
        List<UserModel> matchUsers = new ArrayList<>(ratingGEQ.size() + ratingLT.size());
        matchUsers.addAll(ratingLT);
        matchUsers.addAll(ratingGEQ);
        final int choice = ThreadLocalRandom.current().nextInt(matchUsers.size());
        return matchUsers.get(choice);
    }

    public List<UserModel> getUsersWithRankAroundMe(Connection dbConn, UserModel centerUser, int count) throws SQLException {
        List<UserWithRanking> usersWithRanking = UserRanks.getInstance().getUsersWithRankAround(centerUser, count);
        List<UserModel> fullUsers = new ArrayList<>(usersWithRanking.size());

        for (UserWithRanking userWithRanking: usersWithRanking) {
            Optional<UserModel> fullUserOpt = usersDAO.getUserById(dbConn, userWithRanking.getUserId());
            if (!fullUserOpt.isPresent()) {
                LOG.error("ERROR - user with ID {} isn't present in word_users table, but it was in the rankings table!", userWithRanking.getUserId());
                continue;
            }
            UserModel fullUser = fullUserOpt.get();
            fullUser.setRank(userWithRanking.getRank());
            fullUsers.add(fullUser);
        }

        return fullUsers;
    }
}
