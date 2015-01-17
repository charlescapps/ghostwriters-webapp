package net.capps.word.game.common;

import com.google.common.collect.Lists;
import net.capps.word.game.tile.Tile;

import java.util.List;

/**
 * Created by charlescapps on 1/16/15.
 */
public class Move {
    private final List<Tile> tilesPlayed;
    private final Pos start;
    private final Dir dir;

    public Move(List<Tile> tilesPlayed, Pos start, Dir dir) {
        this.tilesPlayed = tilesPlayed;
        this.start = start;
        this.dir = dir;
    }

    public Move(String word, Pos start, Dir dir) {
        tilesPlayed = Lists.newArrayList();
        for (int i = 0; i < word.length(); i++) {
            Tile tile = Tile.of(word.charAt(i));
            tilesPlayed.add(tile);
        }
        this.start = start;
        this.dir = dir;
    }

    public List<Tile> getTilesPlayed() {
        return tilesPlayed;
    }

    public Pos getStart() {
        return start;
    }

    public Dir getDir() {
        return dir;
    }
}
