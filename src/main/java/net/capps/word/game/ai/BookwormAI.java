package net.capps.word.game.ai;

import net.capps.word.game.board.Game;
import net.capps.word.game.move.Move;

/**
 * Created by charlescapps on 2/22/15.
 * <p/>
 * Similar to MonkeyAI, but
 * 1) Grab moves try to get as many tiles as possible in both directions from the randomly chosen start tile
 * <p/>
 * 2) Play moves only return a move from the top 75% of words, by length.
 */
public class BookwormAI implements GameAI {
    private static final BookwormAI INSTANCE = new BookwormAI();
    private BookwormAI() { }

    public static BookwormAI getInstance() {
        return INSTANCE;
    }

    private final BestMoveFromRandomSampleAI delegateAI = new BestMoveFromRandomSampleAI(0.5f, 0.6f, 0.05f);

    @Override
    public Move getNextMove(Game game) {
        return delegateAI.getNextMove(game);
    }
}
