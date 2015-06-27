package net.capps.word.game.tile;

import com.google.common.base.Preconditions;

/**
 * Created by charlescapps on 1/12/15.
 *
 * Represents a tile in a player's rack that hasn't been placed on the board yet.
 */

public class RackTile {
    public static final char WILD_RACK_TILE = '*'; // letter takes on this value for "Wildcard" RackTiles
    public static final char SCRY_RACK_TILE = '^';
    private static final LetterPoints LETTER_POINTS = LetterPoints.getInstance();

    private final char letter;

    private RackTile(char letter) {
        Preconditions.checkArgument(isValidRackTile(letter),
                "Rack Tile character must be uppercase alphabetic or '*' for a wild tile.");
        this.letter = letter;
    }

    public static boolean isValidRackTile(char letter) {
        return letter == WILD_RACK_TILE || letter == SCRY_RACK_TILE || LetterUtils.isUppercase(letter);
    }

    public static RackTile of(char letter) {
        return new RackTile(letter);
    }

    public static RackTile wild() {
        return new RackTile(WILD_RACK_TILE);
    }
    
    public static RackTile scry() {
        return new RackTile(SCRY_RACK_TILE);
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

    public boolean isLetter() {
        return LetterUtils.isUppercase(letter);
    }

    /**
     * Whether the tile is an unplayed "wildcard" tile in a player's rack
     */
    public boolean isWild() {
        return WILD_RACK_TILE == letter;
    }

    /**
     * Whether the tile is a "scry" bonus tile
     */
    public boolean isScry() {
        return SCRY_RACK_TILE == letter;
    }

    public Tile toTile(char c) {
        Preconditions.checkArgument(LetterUtils.isUppercase(c));
        if (isWild()) {
            return Tile.wildPlayedTile(c);
        }
        Preconditions.checkArgument(c == letter);
        return Tile.playedTile(c);
    }

    public int getLetterPointValue() {
        if (isWild()) {
            return 1;
        }
        return LETTER_POINTS.getPointValue(letter);
    }

    @Override
    public String toString() {
        return Character.toString(letter);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RackTile)) {
            return false;
        }
        RackTile rackTile = (RackTile)o;
        return rackTile.letter == this.letter;
    }

    @Override
    public int hashCode() {
        return Character.valueOf(letter).hashCode();
    }


}
