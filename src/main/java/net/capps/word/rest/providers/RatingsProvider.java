package net.capps.word.rest.providers;

import com.google.common.base.Optional;
import net.capps.word.db.dao.UsersDAO;
import net.capps.word.game.common.BoardSize;
import net.capps.word.game.common.GameResult;
import net.capps.word.game.ranking.EloRankingComputer;
import net.capps.word.rest.models.UserModel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.String.format;

/**
 * Created by charlescapps on 3/29/15.
 */
public class RatingsProvider {
    private static final RatingsProvider INSTANCE = new RatingsProvider();
    private static final EloRankingComputer eloRankingComputer = EloRankingComputer.getInstance();
    private static final UsersDAO usersDAO = UsersDAO.getInstance();

    public static RatingsProvider getInstance() {
        return INSTANCE;
    }

    private RatingsProvider() { }

    public void updatePlayerRatings(UserModel player1, UserModel player2, GameResult gameResult, BoardSize boardSize, Connection dbConn)
            throws SQLException {
        switch (gameResult) {
            case PLAYER1_WIN:
            case PLAYER2_WIN:
            case TIE:
                final int player1Rating = player1.getRating();
                final int player2Rating = player2.getRating();
                final int player1RatingChange = eloRankingComputer.computeRatingChangeForPlayerA(player1Rating, player2Rating, gameResult, boardSize);
                if (player1RatingChange == 0) {
                    return;
                }
                final int player1NewRating = player1Rating + player1RatingChange;
                final int player2NewRating = player2Rating - player1RatingChange;
                usersDAO.updateUserRating(dbConn, player1.getId(), player1NewRating, gameResult.getPlayer1RecordChange());
                usersDAO.updateUserRating(dbConn, player2.getId(), player2NewRating, gameResult.getPlayer2RecordChange());
            default:
                // Do nothing if the game isn't over due to a win or a tie.

        }
    }

    public List<UserModel> getUsersWithRatingsAroundMe(UserModel user, int count) throws SQLException {
        List<UserModel> ratingGEQ = usersDAO.getUsersWithRatingGEQ(user.getId(), user.getRating(), count);
        List<UserModel> ratingLT = usersDAO.getUsersWithRatingLT(user.getRating(), count);
        List<UserModel> resultUsers = new ArrayList<>(ratingGEQ.size() + ratingLT.size() + 1);
        resultUsers.addAll(ratingGEQ);
        resultUsers.add(user);
        resultUsers.addAll(ratingLT);
        return resultUsers;
    }

    public UserModel getBestMatch(UserModel user) throws SQLException {
        final int numAdjacentUsers = 10;
        List<UserModel> ratingGEQ = usersDAO.getUsersWithRatingGEQ(user.getId(), user.getRating(), numAdjacentUsers);
        List<UserModel> ratingLT = usersDAO.getUsersWithRatingLT(user.getRating(), numAdjacentUsers);
        List<UserModel> resultUsers = new ArrayList<>(ratingGEQ.size() + ratingLT.size());
        resultUsers.addAll(ratingGEQ);
        resultUsers.addAll(ratingLT);
        final int choice = ThreadLocalRandom.current().nextInt(resultUsers.size());
        return  resultUsers.get(choice);
    }

    public List<UserModel> getUsersWithRankAroundMe(UserModel centerUser, int count) throws SQLException {
        Optional<UserModel> userWithRankOpt = usersDAO.getUserWithRank(centerUser.getId());
        if (!userWithRankOpt.isPresent()) {
            throw new IllegalStateException(format("Error - user %s not found in the ranks view.", centerUser.toString()));
        }
        UserModel userWithRank = userWithRankOpt.get();
        List<UserModel> usersLT = usersDAO.getUsersWithRankLT(userWithRank.getRank(), count);
        List<UserModel> usersGEQ = usersDAO.getUsersWithRankGT(userWithRank.getRank(), count);
        List<UserModel> results = new ArrayList<>(usersLT.size() + usersGEQ.size() + 1);
        results.addAll(usersLT);
        results.add(userWithRank);
        results.addAll(usersGEQ);
        return results;
    }
}
