package net.capps.word.game.tile;

import com.google.common.base.Preconditions;

/**
 * Created by charlescapps on 1/12/15.
 *
 * Represents a tile on the board.
 */

public class Tile {
    public static final char ABSENT_TILE = '_';
    public static final char WILD_TILE = '*'; // Only used when serializing / deserializing tile sets.

    private final boolean startTile;
    private final boolean wild;
    private final char letter;

    private Tile(char letter, boolean startTile, boolean wild) {
        Preconditions.checkArgument(letter == ABSENT_TILE || LetterUtils.isUppercase(letter),
                "Tile character must be uppercase alphabetic or '_' for an empty tile.");
        this.letter = letter;
        this.startTile = startTile;
        this.wild = wild;
    }

    // ------------- Public Static ------------

    public static Tile startTile(char letter) {
        return new Tile(letter, true, false);
    }

    public static Tile playedTile(char letter) {
        return new Tile(letter, false, false);
    }

    public static Tile wildPlayedTile(char letter) {
        return new Tile(letter, false, true);
    }

    public static Tile absentTile() {
        return new Tile(ABSENT_TILE, false, false);
    }

    public static Tile fromSerializedForm(char c, boolean wild) {
        if (LetterUtils.isUppercase(c)) {
            return new Tile(c, false, wild);
        } else if (LetterUtils.isLowercase(c)) {
            return new Tile(Character.toUpperCase(c), true, wild);
        } else if (c == ABSENT_TILE) {
            return Tile.absentTile();
        }
        throw new IllegalArgumentException(
                String.format("Invalid char for creating tile from serialized form: '%c'", c));
    }

    // ---------- Public ----------

    /**
     * Returns the character for this tile.
     * An absent tile is represented by '_'.
     * Every other tile on a board must have an alphabetic letter.
     */
    public char getLetter() {
        return letter;
    }

    /**
     * Whether the tile is a "wildcard" tile that has been played (or started on the board as wild.)
     */
    public boolean isWild() {
        return wild;
    }

    /**
     * Whether the tile is an empty space on the board.
     */
    public boolean isAbsent() {
        return letter == ABSENT_TILE;
    }

    /**
     * Whether the tile existed at the start of the game.
     * @return true if the tile was on the board at the start of the game, false if it was played by a player.
     */
    public boolean isStartTile() {
        return startTile;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (wild) {
            sb.append('*');
        }
        if (!startTile || isAbsent()) {
            sb.append(letter);
        } else {
            sb.append(Character.toLowerCase(letter));
        }
        return sb.toString();
    }

    public RackTile toRackTile() {
        if (wild) {
            return RackTile.wild();
        }
        return RackTile.of(letter);
    }

}
