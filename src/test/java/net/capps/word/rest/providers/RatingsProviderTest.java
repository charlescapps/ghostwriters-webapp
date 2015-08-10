package net.capps.word.rest.providers;

import net.capps.word.db.WordDbManager;
import net.capps.word.db.dao.UsersDAO;
import net.capps.word.game.common.*;
import net.capps.word.game.ranking.EloRankingComputer;
import net.capps.word.heroku.SetupHelper;
import net.capps.word.rest.models.GameModel;
import net.capps.word.rest.models.UserModel;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

/**
 * Created by charlescapps on 8/9/15.
 */
public class RatingsProviderTest {
    private static final Logger LOG = LoggerFactory.getLogger(RatingsProviderTest.class);

    @Test
    public void testBookPowerBonus() throws Exception {
        SetupHelper.getInstance().initDictionaryDataStructures();
        SetupHelper.getInstance().initGameDataStructures();
        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            doTestBookPowerBonus(BoardSize.TALL, 1000, 2000, 500, 0, dbConn);
            doTestBookPowerBonus(BoardSize.TALL, 1000, 2000, 250, 0, dbConn);
            doTestBookPowerBonus(BoardSize.TALL, 1000, 2000, 100, 0, dbConn);
            doTestBookPowerBonus(BoardSize.TALL, 1000, 2000, 10, 0, dbConn);
            doTestBookPowerBonus(BoardSize.TALL, 1000, 2000, 5, 0, dbConn);
            doTestBookPowerBonus(BoardSize.TALL, 1000, 2000, 0, 0, dbConn);

            doTestBookPowerBonus(BoardSize.GRANDE, 1000, 2000, 500, 0, dbConn);
            doTestBookPowerBonus(BoardSize.GRANDE, 1000, 2000, 250, 0, dbConn);
            doTestBookPowerBonus(BoardSize.GRANDE, 1000, 2000, 100, 0, dbConn);
            doTestBookPowerBonus(BoardSize.GRANDE, 1000, 2000, 10, 0, dbConn);
            doTestBookPowerBonus(BoardSize.GRANDE, 1000, 2000, 5, 0, dbConn);
            doTestBookPowerBonus(BoardSize.GRANDE, 1000, 2000, 0, 0, dbConn);

            doTestBookPowerBonus(BoardSize.VENTI, 1000, 2000, 500, 0, dbConn);
            doTestBookPowerBonus(BoardSize.VENTI, 1000, 2000, 250, 0, dbConn);
            doTestBookPowerBonus(BoardSize.VENTI, 1000, 2000, 100, 0, dbConn);
            doTestBookPowerBonus(BoardSize.VENTI, 1000, 2000, 10, 0, dbConn);
            doTestBookPowerBonus(BoardSize.VENTI, 1000, 2000, 5, 0, dbConn);
            doTestBookPowerBonus(BoardSize.VENTI, 1000, 2000, 0, 0, dbConn);
        }
    }

    private void doTestBookPowerBonus(BoardSize boardSize, int p1Rating, int p2Rating, int p1Books, int p2Books, Connection dbConn) throws Exception {
        UserModel p1 = createUserWithRating(p1Rating, UsersDAO.getInstance(), dbConn);
        UserModel p2 = createUserWithRating(p2Rating, UsersDAO.getInstance(), dbConn);
        p1.setTokens(p1Books);
        p2.setTokens(p2Books);
        GameModel gameModel = createNewGame(p1, p2, boardSize, dbConn);
        gameModel.setGameResult(GameResult.PLAYER1_WIN);

        RatingsProvider.getInstance().updatePlayerRatings(gameModel, dbConn);

        int p1RatingIncrease = p1.getRating() - p1Rating;
        int p2RatingIncrease = p2.getRating() - p2Rating;

        int p1EloIncrease = EloRankingComputer.getInstance().computeRatingChangeForPlayerA(p1Rating, p2Rating, GameResult.PLAYER1_WIN, boardSize);

        LOG.info("player1 ELO increase: " + p1EloIncrease);
        LOG.info("player1 actual increase: " + p1RatingIncrease);
        if (p1Books >= 500) {
            Assert.assertEquals("Expected player to get a 1.2x rating increase!",
                    (int)Math.ceil(p1EloIncrease * 1.2d), p1RatingIncrease);
        } else if (p1Books >= 250) {
            Assert.assertEquals("Expected player to get a 1.15x rating increase!",
                    (int)Math.ceil(p1EloIncrease * 1.15d), p1RatingIncrease);
        } else if (p1Books >= 100) {
            Assert.assertEquals("Expected player to get a 1.1x rating increase!",
                    (int)Math.ceil(p1EloIncrease * 1.1d), p1RatingIncrease);
        } else if (p1Books >= 10) {
            Assert.assertEquals("Expected player to get a 1.05x rating increase!",
                    (int)Math.ceil(p1EloIncrease * 1.05d), p1RatingIncrease);
        } else {
            Assert.assertEquals("Expected player to get no rating increase!",
                    p1EloIncrease, p1RatingIncrease);
        }

    }

    private GameModel createNewGame(UserModel p1, UserModel p2, BoardSize boardSize, Connection dbConn) throws Exception {
        GameModel inputGame = new GameModel();
        inputGame.setPlayer1(p1.getId());
        inputGame.setPlayer2(p2.getId());
        inputGame.setBoardSize(boardSize);
        inputGame.setBonusesType(BonusesType.FIXED_BONUSES);
        inputGame.setGameDensity(GameDensity.REGULAR);
        inputGame.setGameType(GameType.TWO_PLAYER);
        inputGame.setPlayer1Rack("");
        inputGame.setPlayer2Rack("");
        return GamesProvider.getInstance().createNewGame(inputGame, p1, dbConn);
    }

    private UserModel createUserWithRating(int rating, UsersDAO usersDAO, Connection dbConn) throws Exception {
        final String username = "User_" + "_" + RandomStringUtils.randomAlphanumeric(5);
        UserModel inputUser = new UserModel(null, username, null, null, null, false);
        UserModel createdUser = usersDAO.insertNewUser(dbConn, inputUser);

        usersDAO.updateUserRating(dbConn, createdUser.getId(), rating, UserRecordChange.INCREASE_WINS);
        createdUser.setRating(rating);
        return createdUser;
    }
}
