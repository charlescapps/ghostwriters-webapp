package net.capps.word.game.move;

import net.capps.word.game.common.Dir;
import net.capps.word.game.common.Pos;
import net.capps.word.game.tile.RackTile;

import java.util.List;

/**
 * Created by charlescapps on 1/16/15.
 */
public class PlayMove implements Move {
    private final String word;
    private final Pos start;
    private final Dir dir;
    private final List<RackTile> tilesPlayed;

    public PlayMove(String word, Pos start, Dir dir, List<RackTile> tilesPlayed) {
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

    @Override
    public MoveType getMoveType() {
        return MoveType.PLAY_TILES;
    }
}
