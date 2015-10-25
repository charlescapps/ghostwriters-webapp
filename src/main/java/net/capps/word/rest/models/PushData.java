package net.capps.word.rest.models;

/**
 * Created by charlescapps on 4/10/15.
 */
public class PushData {
    private String updatedGameId;
    private String isGameOffer;
    // These are used for game offer push notifications, since this is the data needed to populate the accept game scene.
    private String boardSize;
    private String specialDict;
    private String gameDensity;
    private String bonusesType;
    private String targetUserId;

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

    public String getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(String boardSize) {
        this.boardSize = boardSize;
    }

    public String getSpecialDict() {
        return specialDict;
    }

    public void setSpecialDict(String specialDict) {
        this.specialDict = specialDict;
    }

    public String getGameDensity() {
        return gameDensity;
    }

    public void setGameDensity(String gameDensity) {
        this.gameDensity = gameDensity;
    }

    public String getBonusesType() {
        return bonusesType;
    }

    public void setBonusesType(String bonusesType) {
        this.bonusesType = bonusesType;
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }
}
