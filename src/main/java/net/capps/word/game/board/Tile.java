package net.capps.word.game.board;

import com.google.common.collect.ImmutableMap;
import net.capps.word.exceptions.InvalidTileCharException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by charlescapps on 1/12/15.
 */

public enum Tile {
    ABSENT('_'), WILD('*'),
    A('A'), B('B'), C('C'), D('D'), E('E'), F('F'), G('G'), H('H'), I('I'), J('J'), K('K'), L('L'), M('M'),
    N('N'), O('O'), P('P'), Q('Q'), R('R'), S('S'), T('T'), U('U'), V('V'), W('W'), X('X'), Y('Y'), Z('Z');

    private static final Map<Character, Tile> CHAR_TO_TILE;

    // Build a map from Character --> Tile
    static {
        ImmutableMap.Builder<Character, Tile> builder = ImmutableMap.builder();
        for (Tile tile: Tile.values()) {
            builder.put(tile.getLetter(), tile);
        }
        CHAR_TO_TILE = builder.build();
    }

    private final char letter;

    private Tile(char letter) {
        this.letter = letter;
    }

    public char getLetter() {
        return letter;
    }

    public static Tile valueOf(char c) {
        Tile tile = CHAR_TO_TILE.get(c);
        if (tile == null) {
            throw new InvalidTileCharException("Invalid character for a game tile: " + c);
        }
        return tile;
    }

}
