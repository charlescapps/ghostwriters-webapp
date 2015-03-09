package net.capps.word.game.board;

import com.google.common.collect.ImmutableMap;
import net.capps.word.exceptions.InvalidSquareCharException;

import java.util.Map;

/**
 * Created by charlescapps on 1/12/15.
 */
public enum Square {
    MINE('0', 0),
    NORMAL('1', 1),
    DOUBLE_LETTER('2', 2),
    TRIPLE_LETTER('3', 3),
    QUAD_LETTER('4', 4);

    private static final Map<Character, Square> CHAR_TO_SQUARE;

    static {
        ImmutableMap.Builder<Character, Square> builder = ImmutableMap.builder();
        for (Square square: Square.values()) {
            builder.put(square.getCharRep(), square);
        }
        CHAR_TO_SQUARE = builder.build();
    }

    private final char charRep;
    private final int letterMultiplier;

    private Square(char charRep, int letterMultiplier) {
        this.charRep = charRep;
        this.letterMultiplier = letterMultiplier;
    }

    public char getCharRep() {
        return charRep;
    }

    public int getLetterMultiplier() {
        return letterMultiplier;
    }

    public static Square valueOf(char c) {
        Square square = CHAR_TO_SQUARE.get(c);
        if (square == null) {
            throw new InvalidSquareCharException("Invalid character for board square: " + c);
        }
        return square;
    }
}
