package net.capps.word.game.tile;

/**
 * Created by charlescapps on 1/16/15.
 */
public class AbsentTile extends Tile {
    public static final char ABSENT_CHAR = '_';

    public static final AbsentTile ABSENT_TILE = new AbsentTile();

    private AbsentTile() {

    }

    @Override
    public char getLetter() {
        return ABSENT_CHAR;
    }

    @Override
    public boolean isWild() {
        return false;
    }

    @Override
    public boolean isAbsent() {
        return true;
    }
}
