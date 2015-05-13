package net.capps.word.game.ai;

import net.capps.word.game.board.FixedLayouts;
import net.capps.word.game.board.Game;
import net.capps.word.game.board.SquareSet;
import net.capps.word.game.board.TileSet;
import net.capps.word.game.common.BoardSize;
import net.capps.word.game.common.GameDensity;
import net.capps.word.game.common.GameResult;
import net.capps.word.game.gen.DefaultGameGenerator;
import net.capps.word.game.gen.DefaultSquareSetGenerator;
import net.capps.word.game.gen.GameGenerator;
import net.capps.word.game.gen.SquareSetGenerator;
import net.capps.word.game.move.Move;
import net.capps.word.heroku.SetupHelper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Created by charlescapps on 2/22/15.
 */
public class RandomAITest {
    private static final GameGenerator GG = DefaultGameGenerator.getInstance();
    private static final SquareSetGenerator LG = new DefaultSquareSetGenerator();
    private static final RandomAI RA = RandomAI.getInstance();

    private static final Logger LOG = LoggerFactory.getLogger(RandomAITest.class);

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
        TileSet tileSet = GG.generateRandomFinishedGame(bs.getN(), gameDensity.getNumWords(bs), bs.getN());
        SquareSet squareSet = randomSquares ?
                LG.generateRandomBonusLayout(bs) :
                FixedLayouts.getInstance().getFixedLayout(bs);

        Game game = new Game(0, GameResult.IN_PROGRESS, tileSet, squareSet, "", "", 0, 0, true, Optional.empty());

        while (game.getGameResult() == GameResult.IN_PROGRESS) {
            LOG.info("Game state:\n{}", game);
            Move move = RA.getNextMove(game);

            LOG.info("Generated move: {}", move);

            Optional<String> moveError = game.getMoveError(move);
            if (moveError.isPresent()) {
                LOG.error("Invalid move generated: {}", moveError.get());
            }
            Assert.assertTrue("Expected move to be valid", !moveError.isPresent());
            game.playMove(move);
        }

        LOG.info("Final game state:\n{}", game);
        int player1Points = game.getPlayer1Points();
        int player2Points = game.getPlayer2Points();
        if (player1Points > player2Points) {
            Assert.assertTrue("Player1 should win since they have more points", game.getGameResult() == GameResult.PLAYER1_WIN);
        }
        else if (player1Points < player2Points) {
            Assert.assertTrue("Player2 should win since they have more points", game.getGameResult() == GameResult.PLAYER2_WIN);
        }
        else {
            Assert.assertTrue("Draw since players have equal points", game.getGameResult() == GameResult.TIE);
        }

    }
}
