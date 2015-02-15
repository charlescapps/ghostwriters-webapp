package net.capps.word.rest.models;

import com.google.common.base.MoreObjects;
import net.capps.word.game.common.BoardSize;
import net.capps.word.game.common.BonusesType;
import net.capps.word.game.common.GameDensity;
import net.capps.word.game.common.GameResult;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by charlescapps on 1/18/15.
 */
@XmlRootElement
public class GameModel {
    private Integer id;
    private Integer player1;
    private Integer player2;
    private UserModel player2Model;
    private String player1Rack;
    private String player2Rack;
    private Integer player1Points;
    private Integer player2Points;
    private BoardSize boardSize;
    private BonusesType bonusesType;
    private GameDensity gameDensity;
    private String squares;
    private String tiles;
    private GameResult gameResult;
    private Boolean player1Turn;
    private Long dateCreated;

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

    public UserModel getPlayer2Model() {
        return player2Model;
    }

    public void setPlayer2Model(UserModel player2Model) {
        this.player2Model = player2Model;
    }

    public String getPlayer1Rack() {
        return player1Rack;
    }

    public void setPlayer1Rack(String player1Rack) {
        this.player1Rack = player1Rack;
    }

    public String getPlayer2Rack() {
        return player2Rack;
    }

    public void setPlayer2Rack(String player2Rack) {
        this.player2Rack = player2Rack;
    }

    public Integer getPlayer1Points() {
        return player1Points;
    }

    public void setPlayer1Points(Integer player1Points) {
        this.player1Points = player1Points;
    }

    public Integer getPlayer2Points() {
        return player2Points;
    }

    public void setPlayer2Points(Integer player2Points) {
        this.player2Points = player2Points;
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

    public GameResult getGameResult() {
        return gameResult;
    }

    public void setGameResult(GameResult gameResult) {
        this.gameResult = gameResult;
    }

    public Boolean getPlayer1Turn() {
        return player1Turn;
    }

    public void setPlayer1Turn(Boolean player1Turn) {
        this.player1Turn = player1Turn;
    }

    public Long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Long dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("player1", player1)
                .add("player2", player2)
                .add("player1Rack", player1Rack)
                .add("player2Rack", player2Rack)
                .add("player1Points", player1Points)
                .add("player2Points", player2Points)
                .add("boardSize", boardSize)
                .add("bonusesType", bonusesType)
                .add("gameDensity", gameDensity)
                .add("squares", squares)
                .add("tiles", tiles)
                .add("gameResult", gameResult)
                .add("player1Turn", player1Turn)
                .toString();
    }
}
