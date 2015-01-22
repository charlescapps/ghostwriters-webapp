package net.capps.word.game.common;

import net.capps.word.game.tile.RackTile;

import java.util.List;

/**
 * Created by charlescapps on 1/16/15.
 */
public class Move {
    private final String word;
    private final Pos start;
    private final Dir dir;
    private final List<RackTile> tilesPlayed;

    public Move(String word, Pos start, Dir dir, List<RackTile> tilesPlayed) {
        this.word = word;
        this.start = start;
        this.dir = dir;
        this.tilesPlayed = tilesPlayed;
    }

    public List<RackTile> getTilesPlayed() {
        return tilesPlayed;
    }

    public String getWord() {
        return word;
    }

    public Pos getStart() {
        return start;
    }

    public Dir getDir() {
        return dir;
    }

    public String toSerializedForm() {
        StringBuilder sb = new StringBuilder();
        sb.append(word).append(',')
                .append(start.toSerializedForm())
                .append(',')
                .append(dir.toString())
                .append(',');
        for (RackTile rackTile: tilesPlayed) {
            sb.append(rackTile.getLetter());
        }
        return sb.toString();
    }

    public static Move fromSerializedForm(String str) {
        
    }
}
