package net.capps.word.rest.models;

import net.capps.word.game.common.Dir;
import net.capps.word.game.dict.DictType;
import net.capps.word.game.move.MoveType;

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
    private DictType dict;

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

    public DictType getDict() {
        return dict;
    }

    public void setDict(DictType dict) {
        this.dict = dict;
    }
}
