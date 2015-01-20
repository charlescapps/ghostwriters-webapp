package net.capps.word.game.gen;

import net.capps.word.game.board.Square;
import net.capps.word.game.board.SquareSet;

/**
 * Created by charlescapps on 1/19/15.
 */
public class DefaultLayoutGenerator implements LayoutGenerator {
    @Override
    public SquareSet generateRandomBonusLayout(int N, int dl, int tl, int dw, int tw) {
        SquareSet squareSet = new SquareSet(N);

    }

    private void addRandomBonusSquares(int num, Square type, SquareSet squareSet) {
        
    }
}
