package net.capps.word.game.gen;

import net.capps.word.game.board.SquareSet;
import net.capps.word.game.common.BoardSize;

/**
 * Created by charlescapps on 1/15/15.
 */
public interface SquareSetGenerator {
    SquareSet generateRandomBonusLayout(int N, int dl, int tl, int dw, int tw);
    SquareSet generateRandomBonusLayout(BoardSize boardSize);
}
