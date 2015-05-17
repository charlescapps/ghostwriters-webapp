package net.capps.word.rest.models;

/**
 * Created by charlescapps on 4/10/15.
 */
public class PushData {
    private String updatedGameId;
    private String isGameOffer;

    public PushData() {

    }

    public PushData(String updatedGameId) {
        this.updatedGameId = updatedGameId;
    }

    public String getUpdatedGameId() {
        return updatedGameId;
    }

    public void setUpdatedGameId(String updatedGameId) {
        this.updatedGameId = updatedGameId;
    }

    public String getIsGameOffer() {
        return isGameOffer;
    }

    public void setIsGameOffer(String isGameOffer) {
        this.isGameOffer = isGameOffer;
    }
}
