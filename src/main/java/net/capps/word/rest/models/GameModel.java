package net.capps.word.rest.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.MoreObjects;
import net.capps.word.game.common.*;
import net.capps.word.game.dict.SpecialDict;

import java.util.List;

/**
 * Created by charlescapps on 1/18/15.
 */
public class GameModel {
    public static final GameModel EMPTY_GAME = new GameModel();

    private Integer id;
    private GameType gameType;
    private AiType aiType;
    private Integer player1;
    private Integer player2;
    private UserModel player1Model;
    private UserModel player2Model;
    private String player1Rack;
    private String player2Rack;
    private Integer player1Points;
    private Integer player2Points;
    private BoardSize boardSize;
    private Integer numRows;
    private BonusesType bonusesType;
    private GameDensity gameDensity;
    private String squares;
    private String tiles;
    private GameResult gameResult;
    private Boolean player1Turn;
    private Integer moveNum;
    private Long lastActivity;
    private Long dateCreated;
    private List<MoveModel> lastMoves;
    private MoveModel myMove;
    private Integer player1RatingIncrease;
    private Integer player2RatingIncrease;
    private SpecialDict specialDict;

    public GameModel() {

    }

    public Integer getId() {
        return id;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public AiType getAiType() {
        return aiType;
    }

    public void setAiType(AiType aiType) {
        this.aiType = aiType;
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

    public UserModel getPlayer1Model() {
        return player1Model;
    }

    public void setPlayer1Model(UserModel player1Model) {
        this.player1Model = player1Model;
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
        this.numRows = boardSize.getN();
    }

    public Integer getNumRows() {
        return numRows;
    }

    public void setNumRows(Integer numRows) {
        this.numRows = numRows;
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

    public Integer getMoveNum() {
        return moveNum;
    }

    public void setMoveNum(Integer moveNum) {
        this.moveNum = moveNum;
    }

    public Long getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Long lastActivity) {
        this.lastActivity = lastActivity;
    }

    public Long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Long dateCreated) {
        this.dateCreated = dateCreated;
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public List<MoveModel> getLastMoves() {
        return lastMoves;
    }

    public void setLastMoves(List<MoveModel> lastMoves) {
        this.lastMoves = lastMoves;
    }

    public MoveModel getMyMove() {
        return myMove;
    }

    public void setMyMove(MoveModel myMove) {
        this.myMove = myMove;
    }

    public Integer getPlayer1RatingIncrease() {
        return player1RatingIncrease;
    }

    public void setPlayer1RatingIncrease(Integer player1RatingIncrease) {
        this.player1RatingIncrease = player1RatingIncrease;
    }

    public Integer getPlayer2RatingIncrease() {
        return player2RatingIncrease;
    }

    public void setPlayer2RatingIncrease(Integer player2RatingIncrease) {
        this.player2RatingIncrease = player2RatingIncrease;
    }

    public SpecialDict getSpecialDict() {
        return specialDict;
    }

    public void setSpecialDict(SpecialDict specialDict) {
        this.specialDict = specialDict;
    }

    @JsonIgnore
    public int getCurrentPlayerId() {
        if (player1Turn == null) {
            throw new IllegalStateException("player1Turn field is null!");
        }
        return player1Turn ? player1 : player2;
    }

    @JsonIgnore
    public String getCurrentPlayerRack() {
        if (player1Turn == null) {
            throw new IllegalStateException("player1Turn field is null!");
        }
        return player1Turn ? player1Rack : player2Rack;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("gameType", gameType)
                .add("aiType", aiType)
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
                .add("lastMoves", lastMoves)
                .add("specialDict", specialDict)
                .toString();
    }
}
