package net.capps.word.game.tile;

import com.google.common.base.Preconditions;

/**
 * Created by charlescapps on 1/12/15.
 *
 * Represents a tile in a player's rack that hasn't been placed on the board yet.
 */

public class RackTile {
    public static final char WILD_RACK_TILE = '*'; // letter takes on this value for "Wildcard" RackTiles

    private final char letter;

    private RackTile(char letter) {
        Preconditions.checkArgument(letter == WILD_RACK_TILE || LetterUtils.isUppercase(letter),
                "Rack Tile character must be uppercase alphabetic or '*' for a wild tile.");
        this.letter = letter;
    }

    public static RackTile of(char letter) {
        return new RackTile(letter);
    }

    public static RackTile wild() {
        return new RackTile(WILD_RACK_TILE);
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
     * Whether the tile is an unplayed "wildcard" tile in a player's rack
     */
    public boolean isWild() {
        return WILD_RACK_TILE == letter;
    }

    @Override
    public String toString() {
        return Character.toString(letter);
    }


}
