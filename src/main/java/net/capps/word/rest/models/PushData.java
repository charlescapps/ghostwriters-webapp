package net.capps.word.rest.models;

/**
 * Created by charlescapps on 4/10/15.
 */
public class PushData {
    private String updatedGame;
    private String isGameOffer;

    public PushData() {

    }

    public PushData(String updatedGame) {
        this.updatedGame = updatedGame;
    }

    public String getUpdatedGame() {
        return updatedGame;
    }

    public void setUpdatedGame(String updatedGame) {
        this.updatedGame = updatedGame;
    }

    public String getIsGameOffer() {
        return isGameOffer;
    }

    public void setIsGameOffer(String isGameOffer) {
        this.isGameOffer = isGameOffer;
    }
}