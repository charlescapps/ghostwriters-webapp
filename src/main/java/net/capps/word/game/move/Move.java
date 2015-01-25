package net.capps.word.game.move;

import com.google.common.base.Preconditions;
import net.capps.word.game.common.Dir;
import net.capps.word.game.common.Placement;
import net.capps.word.game.common.Pos;
import net.capps.word.game.common.Rack;
import net.capps.word.game.tile.RackTile;
import net.capps.word.rest.models.MoveModel;

import java.util.List;

/**
 * Created by charlescapps on 1/16/15.
 */
public class Move {
    private final Integer gameId;
    private final String letters;
    private final Pos start;
    private final Dir dir;
    private final List<RackTile> tiles;
    private final MoveType moveType;

    public Move(Integer gameId, MoveType moveType, String letters, Pos start, Dir dir, List<RackTile> tiles) {
        this.gameId = gameId;
        this.moveType = moveType;
        this.letters = letters;
        this.start = start;
        this.dir = dir;
        this.tiles = tiles;
    }

    public Move(MoveModel moveModel) {
        this.gameId = Preconditions.checkNotNull(moveModel.getGameId());
        this.moveType = Preconditions.checkNotNull(moveModel.getMoveType());
        this.letters = Preconditions.checkNotNull(moveModel.getLetters());
        this.start = Preconditions.checkNotNull(moveModel.getStart());
        this.dir = Preconditions.checkNotNull(moveModel.getDir());
        this.tiles = Rack.lettersToTiles(moveModel.getLetters());
    }


    public Integer getGameId() {
        return gameId;
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

    public List<RackTile> getTiles() {
        return tiles;
    }

    public String getTilesAsString() {
        StringBuilder sb = new StringBuilder();
        for (RackTile tile: tiles) {
            sb.append(tile.getLetter());
        }
        return sb.toString();
    }

    public MoveType getMoveType() {
        return moveType;
    }

    public Placement getPlacement() {
        return new Placement(letters, start, dir);
    }
}
