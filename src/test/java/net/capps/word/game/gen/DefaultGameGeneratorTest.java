package net.capps.word.game.gen;

import net.capps.word.constants.WordConstants;
import net.capps.word.game.board.TileSet;
import net.capps.word.game.common.GameSize;
import net.capps.word.game.dict.DictionarySet;
import net.capps.word.game.dict.DictionaryTrie;
import net.capps.word.heroku.SetupHelper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Created by charlescapps on 1/18/15.
 */
public class DefaultGameGeneratorTest {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultGameGeneratorTest.class);

    @BeforeClass
    public static void initDict() throws Exception {
        SetupHelper.getInstance().initDictionary();
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
}
