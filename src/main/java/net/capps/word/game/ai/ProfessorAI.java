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
public class ProfessorAI implements GameAI {
    private static final ProfessorAI TALL_INSTANCE = new ProfessorAI(BoardSize.TALL);
    private static final ProfessorAI GRANDE_INSTANCE = new ProfessorAI(BoardSize.GRANDE);
    private static final ProfessorAI VENTI_INSTANCE = new ProfessorAI(BoardSize.VENTI);

    private ProfessorAI(BoardSize boardSize) {
        delegateAI = new BestMoveFromRandomSampleAI(
                AiType.PROFESSOR_AI.getBoardSearchFraction(boardSize),
                0.8f,
                0.15f);
    }

    public static ProfessorAI getInstance(BoardSize boardSize) {
        switch (boardSize) {
            case TALL:
                return TALL_INSTANCE;
            case GRANDE:
                return GRANDE_INSTANCE;
            case VENTI:
                return VENTI_INSTANCE;
        }
        throw new IllegalArgumentException("Invalid board size: " + boardSize);

    }

    private final BestMoveFromRandomSampleAI delegateAI;

    @Override
    public Move getNextMove(Game game) {
        return delegateAI.getNextMove(game);
    }
}
