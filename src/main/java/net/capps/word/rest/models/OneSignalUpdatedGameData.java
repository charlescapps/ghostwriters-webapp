package net.capps.word.rest.models;

/**
 * Created by charlescapps on 4/10/15.
 */
public class OneSignalUpdatedGameData {
    private int updatedGame;

    public OneSignalUpdatedGameData() {

    }

    public OneSignalUpdatedGameData(int updatedGame) {
        this.updatedGame = updatedGame;
    }

    public int getUpdatedGame() {
        return updatedGame;
    }

    public void setUpdatedGame(int updatedGame) {
        this.updatedGame = updatedGame;
    }
}
