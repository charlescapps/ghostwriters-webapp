package net.capps.word.game.ai;

import net.capps.word.game.board.Game;
import net.capps.word.game.move.Move;
import net.capps.word.rest.models.MoveModel;

import java.util.List;

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
public class OracleTileAI implements GameAI {
    private final BestMoveFromRandomSampleAI delegateAI;

    public OracleTileAI(List<MoveModel> prevTwoMoves, Integer currentPlayerId) {
        delegateAI = new BestMoveFromRandomSampleAI(1f, 0f, 1f, prevTwoMoves, currentPlayerId);
    }

    @Override
    public Move getNextMove(Game game) {
        return delegateAI.getNextMove(game);
    }
}
