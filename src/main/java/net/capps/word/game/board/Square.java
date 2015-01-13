package net.capps.word.game.board;

import com.google.common.collect.ImmutableMap;
import net.capps.word.exceptions.InvalidSquareCharException;

import java.util.Map;

/**
 * Created by charlescapps on 1/12/15.
 */
public enum Square {
    NORMAL('0', 1, 1),
    DOUBLE_LETTER('1', 2, 1),
    TRIPLE_LETTER('2', 3, 1),
    DOUBLE_WORD('3', 1, 2),
    TRIPLE_WORD('4', 1, 3);

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
    private final int wordMultiplier;

    private Square(char charRep, int letterMultiplier, int wordMultiplier) {
        this.charRep = charRep;
        this.letterMultiplier = letterMultiplier;
        this.wordMultiplier = wordMultiplier;
    }

    public char getCharRep() {
        return charRep;
    }

    public int getLetterMultiplier() {
        return letterMultiplier;
    }

    public int getWordMultiplier() {
        return wordMultiplier;
    }

    public static Square valueOf(char c) {
        Square square = CHAR_TO_SQUARE.get(c);
        if (square == null) {
            throw new InvalidSquareCharException("Invalid character for board square: " + c);
        }
        return square;
    }
}
