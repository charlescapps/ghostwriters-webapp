package net.capps.word.game.ai;

import net.capps.word.db.WordDbManager;
import net.capps.word.db.dao.GamesDAO;
import net.capps.word.db.dao.UsersDAO;
import net.capps.word.game.board.SquareSet;
import net.capps.word.game.board.TileSet;
import net.capps.word.game.common.*;
import net.capps.word.game.move.MoveType;
import net.capps.word.heroku.SetupHelper;
import net.capps.word.rest.models.GameModel;
import net.capps.word.rest.models.MoveModel;
import net.capps.word.rest.models.UserModel;
import net.capps.word.rest.providers.GamesProvider;
import net.capps.word.rest.providers.MovesProvider;
import net.capps.word.rest.providers.SpecialActionsProvider;
import net.capps.word.util.ErrorOrResult;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;
import java.sql.Connection;

/**
 * Created by charlescapps on 8/2/15.
 */
public class OracleTileAITest {
    private static final SetupHelper setupHelper = SetupHelper.getInstance();
    private static final UsersDAO usersDAO = UsersDAO.getInstance();
    private static final MovesProvider movesProvider = MovesProvider.getInstance();
    private static final SpecialActionsProvider sap = SpecialActionsProvider.getInstance();

    @Test
    public void testOracleTileWillNotPlayPreviousGrabMove() throws Exception {
        setupHelper.initDictionaryDataStructures();
        setupHelper.initGameDataStructures();
        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            UserModel p1 = createUser(1, dbConn);
            UserModel p2 = createUser(2, dbConn);
            GameModel game = createGameWithTrivialBoard(dbConn, p1, p2);
            Pair<MoveModel, GameModel> gameAndMove = playTrivialGrabMove(dbConn, game, p1);
            game = gameAndMove.getRight();
            MoveModel grabMove = gameAndMove.getLeft();

            GamesProvider.getInstance().acceptGameOfferAndUpdateRack(game, "", dbConn);
            game = playPassMove(dbConn, game, p2);

            ErrorOrResult<MoveModel> oracleMoveOrErr = sap.getOracleMoveAndUpdateUserRack(game, p1, dbConn);
            Assert.assertTrue("Expected an oracle move to be found", !oracleMoveOrErr.isError());

            MoveModel oracleMove = oracleMoveOrErr.getResultOpt().get();
            verifyOracleMove(oracleMove, grabMove);
        }
    }

    private void verifyOracleMove(MoveModel oracle, MoveModel grab) {
        System.out.println("Oracle move:\n" + oracle);
        MoveModel playMove = grabMoveToPlayMove(grab);
        Assert.assertTrue(!oracle.equals(playMove));
    }

    private MoveModel grabMoveToPlayMove(MoveModel grab) {
        return new MoveModel(grab.getGameId(),
                grab.getPlayerId(),
                MoveType.PLAY_WORD,
                grab.getLetters(),
                grab.getStart(),
                grab.getDir(),
                grab.getTiles(),
                0,
                grab.getDatePlayed());
    }

    private UserModel createUser(int index, Connection dbConn) throws Exception {
        final String username = "User_" + index + "_" + RandomStringUtils.randomAlphanumeric(4);
        UserModel inputUser = new UserModel(null, username, null, null, null, false);
        return usersDAO.insertNewUser(dbConn, inputUser);
    }

    private GameModel createGameWithTrivialBoard(Connection dbConn, UserModel p1, UserModel p2) throws Exception {
        GameModel inputGame = new GameModel();
        inputGame.setPlayer1(p1.getId());
        inputGame.setPlayer2(p2.getId());
        inputGame.setBoardSize(BoardSize.TALL);
        inputGame.setBonusesType(BonusesType.FIXED_BONUSES);
        inputGame.setGameDensity(GameDensity.REGULAR);
        inputGame.setGameType(GameType.TWO_PLAYER);
        inputGame.setPlayer1Rack("^");
        inputGame.setPlayer2Rack("");

        TileSet tileSet = new TileSet(5);
        tileSet.load(new StringReader(
                "OF___\n" +
                "_____\n" +
                "_____\n" +
                "_____\n" +
                "_____"));

        SquareSet squareSet = new SquareSet(5);
        squareSet.load(new StringReader(
                "55111\n" +
                "11111\n" +
                "11111\n" +
                "11111\n" +
                "11111"));

        return GamesDAO.getInstance().createNewGame(dbConn, inputGame, tileSet, squareSet);
    }

    private Pair<MoveModel, GameModel> playTrivialGrabMove(Connection dbConn, GameModel game, UserModel p1) throws Exception {
        MoveModel grabMove = new MoveModel(game.getId(),
                p1.getId(),
                MoveType.GRAB_TILES,
                "OF",
                new Pos(0, 0).toPosModel(),
                Dir.E,
                "OF",
                null,
                null);
        GameModel gameModel = movesProvider.playMove(grabMove, game, dbConn);
        return ImmutablePair.of(grabMove, gameModel);
    }

    private GameModel playPassMove(Connection dbConn, GameModel game, UserModel player) throws Exception {
        MoveModel grabMove = new MoveModel(game.getId(),
                player.getId(),
                MoveType.PASS,
                "",
                new Pos(0, 0).toPosModel(),
                Dir.E,
                "",
                null,
                null);
        return movesProvider.playMove(grabMove, game, dbConn);
    }
}
