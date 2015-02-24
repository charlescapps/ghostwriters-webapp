package net.capps.word.game.ai;

import com.google.common.base.Optional;
import net.capps.word.game.board.FixedLayouts;
import net.capps.word.game.board.GameState;
import net.capps.word.game.board.SquareSet;
import net.capps.word.game.board.TileSet;
import net.capps.word.game.common.BoardSize;
import net.capps.word.game.common.GameDensity;
import net.capps.word.game.common.GameResult;
import net.capps.word.game.gen.DefaultGameGenerator;
import net.capps.word.game.gen.DefaultLayoutGenerator;
import net.capps.word.game.gen.GameGenerator;
import net.capps.word.game.gen.LayoutGenerator;
import net.capps.word.game.move.Move;
import net.capps.word.heroku.SetupHelper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by charlescapps on 2/22/15.
 */
public class RandomAiTest {
    private static final GameGenerator GG = new DefaultGameGenerator();
    private static final LayoutGenerator LG = new DefaultLayoutGenerator();
    private static final RandomAi RA = RandomAi.getInstance();

    private static final Logger LOG = LoggerFactory.getLogger(RandomAiTest.class);

    @BeforeClass
    public static void initDataStructures() throws Exception {
        SetupHelper.getInstance().initDictionaryDataStructures();
        SetupHelper.getInstance().initGameDataStructures();
    }

    @Test
    public void testPlayRandomMovesAllGameTypes() {
        for (BoardSize bs: BoardSize.values()) {
            for (GameDensity gameDensity: GameDensity.values()) {
                doTestPlayRandomMoves(bs, gameDensity, true);
                doTestPlayRandomMoves(bs, gameDensity, false);
            }
        }

    }

    private void doTestPlayRandomMoves(BoardSize bs, GameDensity gameDensity, boolean randomSquares ) {
        TileSet tileSet = GG.generateRandomFinishedGame(bs.getN(), gameDensity.getNumWords(bs), bs.getMaxInitialWordSize());
        SquareSet squareSet = randomSquares ?
                LG.generateRandomBonusLayout(bs) :
                FixedLayouts.getInstance().getFixedLayout(bs);

        GameState gameState = new GameState(0, GameResult.IN_PROGRESS, tileSet, squareSet, "", "", 0, 0, true, Optional.<Move>absent());

        while (gameState.getGameResult() == GameResult.IN_PROGRESS) {
            LOG.info("Game state:\n{}", gameState);
            Move move = RA.getNextMove(gameState);

            LOG.info("Generated move: {}", move);

            Optional<String> moveError = gameState.getMoveError(move);
            if (moveError.isPresent()) {
                LOG.error("Invalid move generated: {}", moveError.get());
            }
            Assert.assertTrue("Expected move to be valid", !moveError.isPresent());
            gameState.playMove(move);
        }

        LOG.info("Final game state:\n{}", gameState);
        int player1Points = gameState.getPlayer1Points();
        int player2Points = gameState.getPlayer2Points();
        if (player1Points > player2Points) {
            Assert.assertTrue("Player1 should win since they have more points", gameState.getGameResult() == GameResult.PLAYER1_WIN);
        }
        else if (player1Points < player2Points) {
            Assert.assertTrue("Player2 should win since they have more points", gameState.getGameResult() == GameResult.PLAYER2_WIN);
        }
        else {
            Assert.assertTrue("Draw since players have equal points", gameState.getGameResult() == GameResult.DRAW);
        }

    }
}
