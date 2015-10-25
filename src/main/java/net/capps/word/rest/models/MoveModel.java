package net.capps.word.rest.models;

import com.google.common.base.MoreObjects;
import net.capps.word.game.common.Dir;
import net.capps.word.game.move.MoveType;

import java.util.List;
import java.util.Objects;

/**
 * Created by charlescapps on 1/16/15.
 */
public class MoveModel {
    private Integer gameId;
    private Integer playerId;
    private String letters;
    private PosModel start;
    private Dir dir;
    private String tiles;
    private MoveType moveType;
    private Integer points;
    private Long datePlayed;
    private List<String> specialWordsPlayed;

    public MoveModel() {

    }

    public MoveModel(Integer gameId, Integer playerId, MoveType moveType, String letters, PosModel start, Dir dir, String tiles, Integer points, Long datePlayed) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.moveType = moveType;
        this.letters = letters;
        this.start = start;
        this.dir = dir;
        this.tiles = tiles;
        this.points = points;
        this.datePlayed = datePlayed;
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    public String getLetters() {
        return letters;
    }

    public void setLetters(String letters) {
        this.letters = letters;
    }

    public PosModel getStart() {
        return start;
    }

    public void setStart(PosModel start) {
        this.start = start;
    }

    public Dir getDir() {
        return dir;
    }

    public void setDir(Dir dir) {
        this.dir = dir;
    }

    public String getTiles() {
        return tiles;
    }

    public void setTiles(String tiles) {
        this.tiles = tiles;
    }

    public MoveType getMoveType() {
        return moveType;
    }

    public void setMoveType(MoveType moveType) {
        this.moveType = moveType;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Long getDatePlayed() {
        return datePlayed;
    }

    public void setDatePlayed(Long datePlayed) {
        this.datePlayed = datePlayed;
    }

    public List<String> getSpecialWordsPlayed() {
        return specialWordsPlayed;
    }

    public void setSpecialWordsPlayed(List<String> specialWordsPlayed) {
        this.specialWordsPlayed = specialWordsPlayed;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("gameId", gameId)
                .add("moveType", moveType)
                .add("start", start)
                .add("dir", dir)
                .add("letters", letters)
                .add("tiles", tiles)
                .add("datePlayed", datePlayed)
                .toString();
    }

    /**
     * Define move equality in terms of the content of the move,
     * ignoring game ID.
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MoveModel)) {
            return false;
        }
        MoveModel oMove = (MoveModel)o;
        if (moveType != oMove.moveType) {
            return false;
        }
        switch (moveType) {
            case PLAY_WORD:
            case GRAB_TILES:
                return Objects.equals(start, oMove.start) &&
                        dir == oMove.dir &&
                        Objects.equals(letters, oMove.letters) &&
                        Objects.equals(tiles, oMove.tiles);
            case PASS:
            case RESIGN:
                return true;

        }
        return false;
    }

    @Override
    public int hashCode() {
        switch (moveType) {
            case PASS:
                return 1;
            case RESIGN:
                return 2;
            case GRAB_TILES:
            case PLAY_WORD:
                return Objects.hashCode(letters) ^
                        Objects.hashCode(tiles) ^
                        Objects.hashCode(start) ^
                        Objects.hashCode(dir);
            default:
                return 0;
        }
    }
}
