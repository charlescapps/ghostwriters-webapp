package net.capps.word.game.gen;

import net.capps.word.game.board.TileSet;
import net.capps.word.game.common.GameDensity;
import net.capps.word.heroku.SetupHelper;
import net.capps.word.util.DateUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static net.capps.word.game.common.BoardSize.GRANDE;
import static net.capps.word.game.common.BoardSize.TALL;
import static net.capps.word.game.common.BoardSize.VENTI;

/**
 * Created by charlescapps on 1/18/15.
 */
public class DefaultGameGeneratorTest {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultGameGeneratorTest.class);

    @BeforeClass
    public static void initDict() throws Exception {
        SetupHelper.getInstance().initDictionaryDataStructures();
        SetupHelper.getInstance().initGameDataStructures();
    }

    @Test
    public void testGenerateFirstMove() {
        GameGenerator gg = DefaultGameGenerator.getInstance();
        final int SIZE = 15;
        for (int i = 0; i < 100; i++) {
            TileSet game = gg.generateRandomFinishedGame(SIZE, 1, 8);
            Assert.assertEquals("Game should be correct size", SIZE, game.N);
            LOG.info("\n{}", game);

        }
    }

    @Test
    public void testGenerateGamesWithTwoMoves() {
        GameGenerator gg = DefaultGameGenerator.getInstance();
        final int SIZE = VENTI.getN();
        for (int i = 0; i < 10; i++) {
            TileSet game = gg.generateRandomFinishedGame(SIZE, 2, 8);
            Assert.assertEquals("Game should be correct size", SIZE, game.N);
            LOG.info("\n{}", game);
        }
    }

    @Test
    public void testGenerateGamesWithManyMovesVENTI() {
        doTestGenerateGamesWithManyMoves(VENTI.getN(), VENTI.getN(), GameDensity.REGULAR.getNumWords(VENTI));
    }

    @Test
    public void testGenerateGamesWithManyMovesTALL() {
        doTestGenerateGamesWithManyMoves(TALL.getN(), TALL.getN(), GameDensity.REGULAR.getNumWords(TALL));
    }

    @Test
    public void testGenerateGamesWithManyMovesGRANDE() {
        doTestGenerateGamesWithManyMoves(GRANDE.getN(), GRANDE.getN(), GameDensity.REGULAR.getNumWords(GRANDE));
    }

    public void doTestGenerateGamesWithManyMoves(int size, int maxWordSize, int numWords) {
        GameGenerator gg = DefaultGameGenerator.getInstance();
        final int NUM_GAMES = 50;
        System.out.println("Printing times to generate VENTI games...");
        System.out.println("Max word size = " + maxWordSize);
        long totalComputeTime = 0;
        for (int i = 0; i < NUM_GAMES; ++i) {
            final long GAME_START = System.currentTimeMillis();
            TileSet game = gg.generateRandomFinishedGame(size, numWords, maxWordSize);
            final long DURATION = System.currentTimeMillis() - GAME_START;
            System.out.println(DURATION);
            totalComputeTime += DURATION;
            Assert.assertEquals("Game should be correct size", size, game.N);
            System.out.println(game.toString() + "\n");
        }

        LOG.info("Duration of testGenerateGamesWithManyMoves: {}", DateUtil.getDurationPretty(totalComputeTime));
        LOG.info("Duration in seconds: {}", (totalComputeTime)/1000);
    }

    @Test
    public void testGenerateVentiGamesWithRegularDensity() {
        final long START = System.currentTimeMillis();
        GameGenerator gg = DefaultGameGenerator.getInstance();
        final int SIZE = VENTI.getN();
        for (int i = 0; i < 100; i++) {
            TileSet game = gg.generateRandomFinishedGame(SIZE, GameDensity.REGULAR.getNumWords(VENTI), VENTI.getN());
            Assert.assertEquals("Game should be correct size", SIZE, game.N);
            //  LOG.info("\n{}", game);
        }
        final long END = System.currentTimeMillis();
        LOG.info("Duration of testGenerateVentiGamesWithRegularDensity: {}", DateUtil.getDurationPretty(END - START));
        LOG.info("Duration in seconds: {}", (END - START)/1000);
    }
}
