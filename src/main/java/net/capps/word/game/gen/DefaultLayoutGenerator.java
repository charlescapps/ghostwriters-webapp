package net.capps.word.game.gen;

import com.google.common.collect.Lists;
import net.capps.word.game.board.Square;
import net.capps.word.game.board.SquareSet;
import net.capps.word.game.common.BoardSize;
import net.capps.word.game.common.Pos;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by charlescapps on 1/19/15.
 */
public class DefaultLayoutGenerator implements LayoutGenerator {

    @Override
    public SquareSet generateRandomBonusLayout(int N, int dl, int tl, int dw, int tw) {
        SquareSet squareSet = new SquareSet(N);
        addRandomBonusSquares(dl, Square.DOUBLE_LETTER, squareSet);
        addRandomBonusSquares(tl, Square.TRIPLE_LETTER, squareSet);
        addRandomBonusSquares(dw, Square.DOUBLE_WORD, squareSet);
        addRandomBonusSquares(tw, Square.TRIPLE_WORD, squareSet);

        return squareSet;
    }

    @Override
    public SquareSet generateRandomBonusLayout(BoardSize bs) {
        return generateRandomBonusLayout(bs.getN(), bs.getDl(), bs.getTl(), bs.getDw(), bs.getTw());
    }

    private void addRandomBonusSquares(int num, Square type, SquareSet squareSet) {
        final Random RANDOM = ThreadLocalRandom.current();
        List<Pos> emptyPositions = Lists.newArrayList();
        for (Pos p: squareSet) {
            if (squareSet.get(p) == Square.NORMAL) {
                emptyPositions.add(p);
            }
        }

        // If we're trying to place more random tiles than we have empty squares, just fill in all empty squares.
        if (emptyPositions.size() <= num) {
            for (Pos p: emptyPositions) {
                squareSet.set(p, type);
            }
            return;
        }

        // Otherwise, place them randomly.
        for (int i = 0; i < num; i++) {
            int index = RANDOM.nextInt(emptyPositions.size());
            Pos chosen = emptyPositions.get(index);
            emptyPositions.remove(index);
            squareSet.set(chosen, type);
        }
    }
}
