package net.capps.word.game.ai;

import net.capps.word.game.board.Game;
import net.capps.word.game.move.Move;

/**
 * Created by charlescapps on 2/22/15.
 * <p/>
 * The "Scry AI" for the powerup Scry Tiles.
 *
 * Examines 90% of positions on the board for the best possible move.
 *
 * Always prefers a play tiles move over trying to grab.
 *
 * Always tries a word from the special dictionary first (if present).
 */
public class ScryTileAI implements GameAI {
    private static final ScryTileAI INSTANCE = new ScryTileAI();
    private ScryTileAI() { }

    public static ScryTileAI getInstance() {
        return INSTANCE;
    }

    private final BestMoveFromRandomSampleAI delegateAI = new BestMoveFromRandomSampleAI(1f, 0f, 1f);

    @Override
    public Move getNextMove(Game game) {
        return delegateAI.getNextMove(game);
    }
}
