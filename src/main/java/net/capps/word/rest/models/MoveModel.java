package net.capps.word.rest.models;

import net.capps.word.game.common.Dir;
import net.capps.word.game.common.Pos;
import net.capps.word.game.move.MoveType;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by charlescapps on 1/16/15.
 */
@XmlRootElement
public class MoveModel {
    private Integer gameId;
    private String letters;
    private Pos start;
    private Dir dir;
    private String tiles;
    private MoveType moveType;
    private Integer points;
    private Long datePlayed;

    public MoveModel() {

    }

    public MoveModel(Integer gameId, MoveType moveType, String letters, Pos start, Dir dir, String tiles, Integer points, Long datePlayed) {
        this.gameId = gameId;
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

    public String getLetters() {
        return letters;
    }

    public void setLetters(String letters) {
        this.letters = letters;
    }

    public Pos getStart() {
        return start;
    }

    public void setStart(Pos start) {
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
}
