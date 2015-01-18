package net.capps.word.game.tile;

import net.capps.word.exceptions.InvalidTileCharException;

/**
 * Created by charlescapps on 1/16/15.
 */
public class WildTile extends Tile {
    public static final char WILD = '*';

    public static final WildTile WILD_UNCHOSEN = new WildTile();

    private final char chosenLetter;

    public WildTile() {
        this.chosenLetter = WILD;
    }

    public WildTile(char c) {
        if (WILD != c && !Character.isAlphabetic(c)) {
            throw new InvalidTileCharException("Cannot create Tile with non-alphabetic char:" + c);
        }
        this.chosenLetter = c;
    }

    @Override
    public char getLetter() {
        return chosenLetter;
    }

    @Override
    public boolean isWild() {
        return true;
    }

    @Override
    public boolean isAbsent() {
        return false;
    }
}
