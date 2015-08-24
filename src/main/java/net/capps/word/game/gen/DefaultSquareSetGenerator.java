package net.capps.word.game.gen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.capps.word.game.board.Square;
import net.capps.word.game.board.SquareSet;
import net.capps.word.game.common.BoardSize;
import net.capps.word.game.common.Pos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by charlescapps on 1/19/15.
 */
public class DefaultSquareSetGenerator implements SquareSetGenerator {
    private static final DefaultSquareSetGenerator INSTANCE = new DefaultSquareSetGenerator();

    public static DefaultSquareSetGenerator getInstance() {
        return INSTANCE;
    }

    private DefaultSquareSetGenerator() {

    }

    private static final int PROB_2_ADJ = 1;
    private static final int PROB_1_ADJ = 10;
    private static final int PROB_0_ADJ = 1000;

    @Override
    public SquareSet generateRandomBonusLayout(int N, int x2, int x3, int x4, int x5) {
        SquareSet squareSet = new SquareSet(N);
        ImmutableList<Pos> emptyPositionsImmutable = PositionLists.getInstance().getPositionList(N);
        List<Pos> emptyPositions = Lists.newArrayList(emptyPositionsImmutable);
        addRandomBonusSquares(x2, Square.DOUBLE_LETTER, squareSet, emptyPositions);
        addRandomBonusSquares(x3, Square.TRIPLE_LETTER, squareSet, emptyPositions);
        addRandomBonusSquares(x4, Square.QUAD_LETTER, squareSet, emptyPositions);
        addRandomBonusSquares(x5, Square.PENTA_LETTER, squareSet, emptyPositions);

        return squareSet;
    }

    @Override
    public SquareSet generateRandomBonusLayout(BoardSize bs) {
        return generateRandomBonusLayout(bs.getN(), bs.getX2(), bs.getX3(), bs.getX4(), bs.getX5());
    }

    private void addOneRandomBonus(Square type, SquareSet squareSet, List<Pos> emptyPositions) {
        final Random RANDOM = ThreadLocalRandom.current();
        final List<Integer> relativeProbs = new ArrayList<>(emptyPositions.size());
        int total = 0;
        for (Pos p: emptyPositions) {
            int numAdjs = squareSet.getNumAdjacentBonuses(p);
            if (numAdjs >= 2) {
                total += PROB_2_ADJ;
                relativeProbs.add(PROB_2_ADJ);
            } else if (numAdjs == 1) {
                total += PROB_1_ADJ;
                relativeProbs.add(PROB_1_ADJ);
            } else {
                total += PROB_0_ADJ;
                relativeProbs.add(PROB_0_ADJ);
            }
        }

        int randomProb = RANDOM.nextInt(total);
        int cumulative = 0;

        for (int i = 0; i < emptyPositions.size(); i++) {
            cumulative += relativeProbs.get(i);
            if (randomProb < cumulative) {
                Pos p = emptyPositions.get(i);
                squareSet.set(p, type);
                emptyPositions.remove(i);
                return;
            }
        }
    }

    private void addRandomBonusSquares(int num, Square type, SquareSet squareSet, List<Pos> emptyPositions) {
        for (int i = 0; i < num; i++) {
            addOneRandomBonus(type, squareSet, emptyPositions);
        }
    }
}
