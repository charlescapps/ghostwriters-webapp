package net.capps.word.game.board;

import net.capps.word.game.common.BoardSize;
import net.capps.word.heroku.SetupHelper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by charlescapps on 1/22/15.
 */
public class FixedLayoutsTest {
    private static final Logger LOG = LoggerFactory.getLogger(FixedLayoutsTest.class);

    @BeforeClass
    public static void beforeClass() throws Exception {
        FixedLayouts.getInstance().initLayouts();
    }

    @Test
    public void testPrintLayouts() {
        for (BoardSize bs: BoardSize.values()) {
            SquareSet layout = FixedLayouts.getInstance().getFixedLayout(bs);
            LOG.info("LAYOUT:\n{}", layout);
        }
    }
}
