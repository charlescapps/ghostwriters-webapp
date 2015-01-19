package net.capps.word.rest.models;

import net.capps.word.game.common.BoardSize;
import net.capps.word.game.common.BonusesType;
import net.capps.word.game.common.GameDensity;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by charlescapps on 1/18/15.
 */
@XmlRootElement
public class GameModel {
    private Integer id;
    private Integer player1;
    private Integer player2;
    private BoardSize boardSize;
    private BonusesType bonusesType;
    private GameDensity gameDensity;
    private String squares;
    private String tiles;

    public GameModel() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPlayer1() {
        return player1;
    }

    public void setPlayer1(Integer player1) {
        this.player1 = player1;
    }

    public Integer getPlayer2() {
        return player2;
    }

    public void setPlayer2(Integer player2) {
        this.player2 = player2;
    }

    public BoardSize getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(BoardSize boardSize) {
        this.boardSize = boardSize;
    }

    public BonusesType getBonusesType() {
        return bonusesType;
    }

    public void setBonusesType(BonusesType bonusesType) {
        this.bonusesType = bonusesType;
    }

    public GameDensity getGameDensity() {
        return gameDensity;
    }

    public void setGameDensity(GameDensity gameDensity) {
        this.gameDensity = gameDensity;
    }

    public String getSquares() {
        return squares;
    }

    public void setSquares(String squares) {
        this.squares = squares;
    }

    public String getTiles() {
        return tiles;
    }

    public void setTiles(String tiles) {
        this.tiles = tiles;
    }
}
