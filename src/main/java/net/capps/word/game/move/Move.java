package net.capps.word.game.move;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import net.capps.word.game.common.Dir;
import net.capps.word.game.common.Placement;
import net.capps.word.game.common.Pos;
import net.capps.word.game.common.Rack;
import net.capps.word.game.tile.LetterPoints;
import net.capps.word.game.tile.RackTile;
import net.capps.word.rest.models.MoveModel;
import net.capps.word.rest.models.PosModel;

import java.util.List;

/**
 * Created by charlescapps on 1/16/15.
 */
public class Move {
    private static final LetterPoints LETTER_POINTS = LetterPoints.getInstance();

    private final Integer gameId;
    private final String letters;
    private final Pos start;
    private final Dir dir;
    private final List<RackTile> tiles;
    private final MoveType moveType;
    private int points;

    public static Move passMove(int gameId) {
        return new Move(gameId, MoveType.PASS, "", Pos.of(0, 0), Dir.E, Lists.<RackTile>newArrayList());
    }

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
        if (this.moveType != MoveType.PASS) {
            PosModel start = Preconditions.checkNotNull(moveModel.getStart());
            this.letters = Preconditions.checkNotNull(moveModel.getLetters());
            this.start = Preconditions.checkNotNull(start.toPos());
            this.dir = Preconditions.checkNotNull(moveModel.getDir());
            this.tiles = Rack.lettersToTiles(moveModel.getTiles());
        } else {
            this.letters = null;
            this.start = null;
            this.dir = null;
            this.tiles = null;
        }
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

    public MoveModel toMoveModel(int playerId, int points) {
        return new MoveModel(gameId,
                             playerId,
                             moveType,
                             letters,
                             start.toPosModel(),
                             dir,
                             getTilesAsString(),
                             points,
                             null);
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int computeSumOfTilePoints() {
        int pointValue = 0;
        for (RackTile rackTile: tiles) {
            pointValue += rackTile.getLetterPointValue();
        }
        return pointValue;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("moveType", moveType)
                .add("gameId", gameId)
                .add("letters", letters)
                .add("start", start)
                .add("dir", dir)
                .add("tiles", tiles)
                .toString();
    }
}
