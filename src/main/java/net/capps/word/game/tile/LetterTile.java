package net.capps.word.game.tile;

import net.capps.word.exceptions.InvalidTileCharException;

/**
 * Created by charlescapps on 1/16/15.
 */
public class LetterTile extends Tile {
    private final char ch;

    public LetterTile(char ch) {
        if (!Character.isAlphabetic(ch)) {
            throw new InvalidTileCharException("Letter tiles can only have alphabetic chars. Invalid char: " + ch);
        }
        this.ch = Character.toUpperCase(ch);
    }

    @Override
    public char getLetter() {
        return ch;
    }

    @Override
    public boolean isWild() {
        return false;
    }

    @Override
    public boolean isAbsent() {
        return false;
    }
}
