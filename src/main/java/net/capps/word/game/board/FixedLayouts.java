package net.capps.word.game.board;

import net.capps.word.exceptions.InvalidBoardException;
import net.capps.word.game.common.BoardSize;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by charlescapps on 1/19/15.
 */
public class FixedLayouts {
    private static final FixedLayouts INSTANCE = new FixedLayouts();

    private SquareSet tinyLayout;
    private SquareSet tallLayout;
    private SquareSet grandeLayout;
    private SquareSet ventiLayout;

    public static FixedLayouts getInstance() {
        return INSTANCE;
    }

    // -------- Constructor --------
    private FixedLayouts() {
    }

    // -------- Public -----------
    public void initLayouts() throws IOException, InvalidBoardException {
        tinyLayout = createFixedLayout(BoardSize.TINY);
        tallLayout = createFixedLayout(BoardSize.TALL);
        grandeLayout = createFixedLayout(BoardSize.GRANDE);
        ventiLayout = createFixedLayout(BoardSize.VENTI);
    }

    public SquareSet getFixedLayout(BoardSize boardSize) {
        switch (boardSize) {
            case TINY: return tinyLayout;
            case TALL: return tallLayout;
            case GRANDE: return grandeLayout;
            case VENTI: return ventiLayout;
        }
        throw new IllegalStateException("Invalid board size: " + boardSize);
    }


    // ------- Private helpers -----

    private SquareSet createFixedLayout(BoardSize boardSize) throws IOException, InvalidBoardException {
        SquareSet squareSet = new SquareSet(boardSize);
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(boardSize.getDefaultLayoutFile());
             InputStreamReader isr = new InputStreamReader(is)) {

            squareSet.load(isr);
        }
        return squareSet;
    }


}
