package net.capps.word.game.tile;

/**
 * Created by charlescapps on 1/12/15.
 */

public abstract class Tile {
    public abstract char getLetter();
    public abstract boolean isWild();
    public abstract boolean isAbsent();

    public static Tile of(char c) {
        if (AbsentTile.ABSENT_CHAR == c) {
            return AbsentTile.ABSENT_TILE;
        }
        if (WildTile.WILD == c) {
            return WildTile.WILD_UNCHOSEN;
        }
        return new LetterTile(c);
    }

    public static Tile absent() {
        return AbsentTile.ABSENT_TILE;
    }

    public static Tile wild() {
        return WildTile.WILD_UNCHOSEN;
    }

    // ---------- Public ----------

    public boolean isPlayable() {
        return Character.isAlphabetic(getLetter());
    }

    @Override
    public String toString() {
        return Character.toString(getLetter());
    }

}
