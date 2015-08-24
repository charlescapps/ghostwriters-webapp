package net.capps.word.game.ai;

import net.capps.word.game.board.Game;
import net.capps.word.game.common.AiType;
import net.capps.word.game.common.BoardSize;
import net.capps.word.game.move.Move;

/**
 * Created by charlescapps on 2/22/15.
 * <p>
 * Similar to MonkeyAI, but
 * 1) Grab moves try to get as many tiles as possible in both directions from the randomly chosen start tile
 * <p>
 * 2) Play moves only return a move from the top 75% of words, by length.
 */
public class BookwormAI implements GameAI {
    private static final BookwormAI TALL_INSTANCE = new BookwormAI(BoardSize.TALL);
    private static final BookwormAI GRANDE_INSTANCE = new BookwormAI(BoardSize.GRANDE);
    private static final BookwormAI VENTI_INSTANCE = new BookwormAI(BoardSize.VENTI);

    private BookwormAI(BoardSize boardSize) {
        delegateAI = new BestMoveFromRandomSampleAI(
                AiType.BOOKWORM_AI.getBoardSearchFraction(boardSize),
                0.6f,
                0.05f);
    }

    public static BookwormAI getInstance(BoardSize boardSize) {
        switch (boardSize) {
            case TALL:
                return TALL_INSTANCE;
            case GRANDE:
                return GRANDE_INSTANCE;
            case VENTI:
                return VENTI_INSTANCE;
        }
        throw new IllegalStateException("Invalid board size: " + boardSize);
    }

    private final BestMoveFromRandomSampleAI delegateAI;

    @Override
    public Move getNextMove(Game game) {
        return delegateAI.getNextMove(game);
    }
}
