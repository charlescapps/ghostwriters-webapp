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
    private final Integer gameId;
    private final String letters;
    private final Pos start;
    private final Dir dir;
    private final String tiles;
    private final MoveType moveType;
    private final Integer points;
    private final Long datePlayed;

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

    public String getLetters() {
        return letters;
    }

    public Pos getStart() {
        return start;
    }

    public Dir getDir() {
        return dir;
    }

    public String getTiles() {
        return tiles;
    }

    public Integer getPoints() {
        return points;
    }

    public MoveType getMoveType() {
        return moveType;
    }

    public Long getDatePlayed() {
        return datePlayed;
    }

}
