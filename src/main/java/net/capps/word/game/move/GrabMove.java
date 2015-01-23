package net.capps.word.game.move;

import net.capps.word.game.common.Dir;
import net.capps.word.game.common.Pos;
import net.capps.word.game.tile.RackTile;

import java.util.List;

/**
 * Created by charlescapps on 1/16/15.
 */
public class GrabMove implements Move {
    private final String letters;
    private final Pos start;
    private final Dir dir;
    private final List<RackTile> tilesGrabbed;

    public GrabMove(String letters, Pos start, Dir dir, List<RackTile> tilesGrabbed) {
        this.letters = letters;
        this.start = start;
        this.dir = dir;
        this.tilesGrabbed = tilesGrabbed;
    }

    public String getLetters() {
        return letters;
    }

    public Pos getStart() {
        return start;
    }

    public Dir getDir() {
        return dir;
    }

    public List<RackTile> getTilesGrabbed() {
        return tilesGrabbed;
    }

    @Override
    public MoveType getMoveType() {
        return MoveType.GRAB_TILES;
    }
}
