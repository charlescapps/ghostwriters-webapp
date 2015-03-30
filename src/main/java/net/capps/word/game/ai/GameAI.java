package net.capps.word.game.ai;

import net.capps.word.game.board.Game;
import net.capps.word.game.move.Move;

/**
 * Created by charlescapps on 2/22/15.
 */
public interface GameAI {
    Move getNextMove(Game game);
}
