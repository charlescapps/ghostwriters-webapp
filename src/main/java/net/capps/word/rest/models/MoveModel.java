package net.capps.word.rest.models;

import net.capps.word.game.common.Dir;
import net.capps.word.game.common.Pos;
import net.capps.word.game.move.MoveType;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

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

    public MoveModel(Integer gameId, MoveType moveType, String letters, Pos start, Dir dir, String tiles) {
        this.gameId = gameId;
        this.moveType = moveType;
        this.letters = letters;
        this.start = start;
        this.dir = dir;
        this.tiles = tiles;
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

    public MoveType getMoveType() {
        return moveType;
    }

}
