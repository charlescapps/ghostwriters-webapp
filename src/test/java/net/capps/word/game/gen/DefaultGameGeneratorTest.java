package net.capps.word.game.gen;

import net.capps.word.game.board.TileSet;
import net.capps.word.game.common.BoardSize;
import net.capps.word.heroku.SetupHelper;
import net.capps.word.util.DateUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;

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
        GameGenerator gg = new DefaultGameGenerator();
        final int SIZE = 15;
        for (int i = 0; i < 100; i++) {
            TileSet game = gg.generateRandomFinishedGame(SIZE, 1, 8);
            Assert.assertEquals("Game should be correct size", SIZE, game.N);
            LOG.info("\n{}", game);

        }
    }

    @Test
    public void testGenerateGamesWithTwoMoves() {
        GameGenerator gg = new DefaultGameGenerator();
        final int SIZE = BoardSize.VENTI.getN();
        for (int i = 0; i < 10; i++) {
            TileSet game = gg.generateRandomFinishedGame(SIZE, 2, 8);
            Assert.assertEquals("Game should be correct size", SIZE, game.N);
            LOG.info("\n{}", game);
        }
    }

    @Test
    public void testGenerateGamesWithManyMoves() {
        final long START = System.currentTimeMillis();
        GameGenerator gg = new DefaultGameGenerator();
        final int SIZE = BoardSize.VENTI.getN();
        for (int numWords = 2; numWords < 40; numWords++) {
            TileSet game = gg.generateRandomFinishedGame(SIZE, numWords, BoardSize.VENTI.getMaxInitialWordSize());
            Assert.assertEquals("Game should be correct size", SIZE, game.N);
            LOG.trace("\n{}", game);
        }
        final long END = System.currentTimeMillis();
        LOG.info("Duration of testGenerateGamesWithManyMoves: {}", DateUtil.getDurationPretty(END - START));
        LOG.info("Duration in seconds: {}", (END - START)/1000);
    }

    @Test
    public void testGenerateWordsForBoardThatHadBug() throws Exception {
        for (int i = 0; i < 200; i++) {
            TileSet tileSet = new TileSet(BoardSize.VENTI.getN());

            try (InputStream is = getClass().getClassLoader().getResourceAsStream("net/capps/word/games/bug.txt")) {
                tileSet.load(new InputStreamReader(is));
            }

            LOG.info("Board:\n{}", tileSet);

            GameGenerator gg = new DefaultGameGenerator();
            gg.generateRandomWord(tileSet, BoardSize.VENTI.getMaxInitialWordSize());

            LOG.info("Board after move:\n{}", tileSet);
        }
    }
}
