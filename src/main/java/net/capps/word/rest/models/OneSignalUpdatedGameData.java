package net.capps.word.rest.models;

/**
 * Created by charlescapps on 4/10/15.
 */
public class OneSignalUpdatedGameData {
    private String updatedGame;

    public OneSignalUpdatedGameData() {

    }

    public OneSignalUpdatedGameData(String updatedGame) {
        this.updatedGame = updatedGame;
    }

    public String getUpdatedGame() {
        return updatedGame;
    }

    public void setUpdatedGame(String updatedGame) {
        this.updatedGame = updatedGame;
    }
}
