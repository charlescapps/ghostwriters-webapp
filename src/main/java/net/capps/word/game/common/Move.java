package net.capps.word.game.common;

import net.capps.word.game.tile.Tile;

import java.util.List;

/**
 * Created by charlescapps on 1/16/15.
 */
public class Move {
    private final List<Tile> tilesPlayed;
    private final String wordPlayed;
    private final Pos start;
    private final Dir dir;

    public Move(List<Tile> tilesPlayed, String wordPlayed, Pos start, Dir dir) {
        this.tilesPlayed = tilesPlayed;
        this.wordPlayed = wordPlayed;
        this.start = start;
        this.dir = dir;
    }

    public List<Tile> getTilesPlayed() {
        return tilesPlayed;
    }

    public String getWordPlayed() {
        return wordPlayed;
    }

    public Pos getStart() {
        return start;
    }

    public Dir getDir() {
        return dir;
    }
}
