package net.capps.word.rest.models;

/**
 * Created by charlescapps on 5/2/15.
 */
public class UserGameSummaryModel {
    private Integer id;
    private Integer numGamesMyTurn;
    private Integer numGamesOffered;

    public UserGameSummaryModel() {
    }

    public UserGameSummaryModel(Integer id, Integer numGamesMyTurn, Integer numGamesOffered) {
        this.id = id;
        this.numGamesMyTurn = numGamesMyTurn;
        this.numGamesOffered = numGamesOffered;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNumGamesMyTurn() {
        return numGamesMyTurn;
    }

    public void setNumGamesMyTurn(Integer numGamesMyTurn) {
        this.numGamesMyTurn = numGamesMyTurn;
    }

    public Integer getNumGamesOffered() {
        return numGamesOffered;
    }

    public void setNumGamesOffered(Integer numGamesOffered) {
        this.numGamesOffered = numGamesOffered;
    }
}
