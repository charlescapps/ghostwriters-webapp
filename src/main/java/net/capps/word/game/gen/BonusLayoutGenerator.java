package net.capps.word.game.gen;

import net.capps.word.game.board.SquareSet;

/**
 * Created by charlescapps on 1/15/15.
 */
public interface BonusLayoutGenerator {
    SquareSet generateRandomBonusLayout(int N, int dl, int tl, int dw, int tw);
}
