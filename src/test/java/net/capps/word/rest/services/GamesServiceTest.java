package net.capps.word.rest.services;

import net.capps.word.db.WordDbManager;
import net.capps.word.game.board.Game;
import net.capps.word.game.board.TileSet;
import net.capps.word.game.common.*;
import net.capps.word.game.move.Move;
import net.capps.word.game.move.MoveType;
import net.capps.word.rest.filters.InitialUserAuthFilter;
import net.capps.word.rest.filters.RegularUserAuthFilter;
import net.capps.word.rest.models.ErrorModel;
import net.capps.word.rest.models.GameModel;
import net.capps.word.rest.models.MoveModel;
import net.capps.word.rest.providers.MovesProvider;
import net.capps.word.util.ErrorOrResult;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import java.sql.Connection;
import java.util.Optional;

/**
 * Created by charlescapps on 1/24/15.
 */
public class GamesServiceTest extends BaseWordServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(GamesServiceTest.class);
    private static final MovesProvider movesProvider = MovesProvider.getInstance();

    @Override
    protected Application configure() {
        return new ResourceConfig(LoginService.class, GamesService.class, MovesService.class,
                RegularUserAuthFilter.class, InitialUserAuthFilter.class);
    }

    @Test
    public void testCreateVentiGame() {
        doTestCreateGame(BoardSize.VENTI, BonusesType.RANDOM_BONUSES, GameDensity.REGULAR);
    }

    @Test
    public void testCreateAllGameTypes() {
        for (BoardSize boardSize: BoardSize.values()) {
            for (BonusesType bonusesType: BonusesType.values()) {
                for (GameDensity gameDensity: GameDensity.values()) {
                    doTestCreateGame(boardSize, bonusesType, gameDensity);
                }
            }
        }
    }

    @Test
    public void testPlayTilesYouGrabbed() throws Exception {
        GameModel gameModel = doTestCreateGame(BoardSize.GRANDE, BonusesType.RANDOM_BONUSES, GameDensity.REGULAR);
        Game game = new Game(gameModel, Optional.empty());

        MoveModel grabMove = findGrabMove(game, gameModel.getPlayer1(), Optional.empty());

        MoveModel player2Move = findGrabMove(game,
                gameModel.getPlayer2(),
                Optional.of(grabMove.getStart().toPos()));

        MoveModel playMove = findPlayMoveFromGrabMove(game, grabMove);

        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {

            // Player 1: Grabs 1 tile
            gameModel = movesProvider.playMove(grabMove, gameModel, dbConn);

            // Player 2: Grabs 1 tile on a different row & column
            gameModel = movesProvider.playMove(player2Move, gameModel, dbConn);

            // Player 1: Play the same tile they just grabbed
            ErrorOrResult<GameModel> errorOrResult = movesProvider.validateMove(playMove, fooUser, dbConn);
            if (errorOrResult.isError()) {
                LOG.info("Play move error: {}", errorOrResult.getErrorOpt().get().getErrorMessage());
            }
            Assert.assertTrue("Expect an error to be present when you try to play the tile you just grabbed.",
                    errorOrResult.isError());
        }
    }

    private MoveModel findGrabMove(Game game, int playerId, Optional<Pos> forbiddenPos) {
        TileSet tileSet = game.getTileSet();
        Pos p = null;
        for (int i = 0; i < tileSet.N; ++i) {
            for (int j = 0; j < tileSet.N; ++j) {
                if (forbiddenPos.isPresent() &&
                        (i == forbiddenPos.get().r || j == forbiddenPos.get().c)) {
                    continue;
                }
                if (tileSet.isOccupied(Pos.of(i, j))) {
                    p = Pos.of(i, j);
                    break;
                }
            }
        }
        if (p == null) {
            throw new IllegalStateException();
        }

        Dir dir;
        if (tileSet.isOccupiedAndValid(p.e()) || tileSet.isOccupiedAndValid(p.w())) {
            dir = Dir.E;
        } else {
            dir = Dir.S;
        }

        String letter = Character.toString(tileSet.getLetterAt(p));
        return new MoveModel(game.getGameId(),
                playerId,
                MoveType.GRAB_TILES,
                letter,
                p.toPosModel(),
                dir,
                letter,
                null,
                null);
    }

    private MoveModel findPlayMoveFromGrabMove(Game game, MoveModel grabMove) {
        final Dir dir = grabMove.getDir();

        TileSet tileSet = game.getTileSet();
        Pos start = tileSet.getEndOfOccupied(grabMove.getStart().toPos(), dir.negate());
        Pos end = tileSet.getEndOfOccupied(grabMove.getStart().toPos(), dir);
        String letters = tileSet.getWordWithMissingChar(start,
                end,
                grabMove.getStart().toPos(),
                grabMove.getLetters().charAt(0));

        LOG.info("Computed play move with letters = \"{}\" and tiles = \"{}\"", letters, grabMove.getTiles());

        return new MoveModel(game.getGameId(),
                grabMove.getPlayerId(),
                MoveType.PLAY_WORD,
                letters,
                start.toPosModel(),
                dir,
                grabMove.getTiles(),
                null,
                null);
    }

    private GameModel doTestCreateGame(BoardSize boardSize, BonusesType bonusesType, GameDensity gameDensity) {
        String cookie = login(fooUser.getUsername(), PASS);
        LOG.info("Cookie={}", cookie);

        GameModel input = new GameModel();
        input.setBoardSize(boardSize);
        input.setBonusesType(bonusesType);
        input.setGameDensity(gameDensity);
        input.setPlayer1(fooUser.getId());
        input.setPlayer2(barUser.getId());
        input.setGameType(GameType.TWO_PLAYER);

        GameModel result = target("games")
                .request()
                .header("Cookie", cookie)
                .build("POST", Entity.entity(input, MediaType.APPLICATION_JSON))
                .invoke(GameModel.class);

        LOG.info("Logging the game result:");
        LOG.info(result.toString());

        Assert.assertEquals(result.getPlayer1(), input.getPlayer1());
        Assert.assertEquals(result.getPlayer2(), input.getPlayer2());
        Assert.assertEquals("", result.getPlayer1Rack());
        Assert.assertEquals("", result.getPlayer2Rack());
        Assert.assertEquals(input.getBoardSize(), result.getBoardSize());
        Assert.assertEquals(input.getBonusesType(), result.getBonusesType());
        Assert.assertEquals(input.getGameDensity(), result.getGameDensity());
        Assert.assertEquals(new Integer(0), result.getPlayer1Points());
        Assert.assertEquals(new Integer(0), result.getPlayer2Points());
        final int N = input.getBoardSize().getN();
        Assert.assertEquals(N * N, result.getSquares().length());
        Assert.assertEquals(N * N, result.getTiles().length());
        Assert.assertTrue(result.getDateCreated() > 0);

        return result;
    }




}
