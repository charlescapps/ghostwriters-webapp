package net.capps.word.rest.models;

/**
 * Created by charlescapps on 4/10/15.
 */
public class OneSignalUpdatedGameData {
    private GameModel updatedGame;

    public OneSignalUpdatedGameData() {

    }

    public OneSignalUpdatedGameData(GameModel updatedGame) {
        this.updatedGame = updatedGame;
    }

    public GameModel getUpdatedGame() {
        return updatedGame;
    }

    public void setUpdatedGame(GameModel updatedGame) {
        this.updatedGame = updatedGame;
    }
}
