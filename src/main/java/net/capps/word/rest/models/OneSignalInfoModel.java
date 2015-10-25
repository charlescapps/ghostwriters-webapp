package net.capps.word.rest.models;

/**
 * Created by charlescapps on 10/25/15.
 * This represents the data the client (app) needs to send to the server after push notifications are
 * successfully registered with OneSignal and a OneSignal player id has been assigned.
 */
public class OneSignalInfoModel {
    private Integer userId;
    private String oneSignalPlayerId;

    public OneSignalInfoModel() {
    }

    public OneSignalInfoModel(Integer userId, String oneSignalPlayerId) {
        this.userId = userId;
        this.oneSignalPlayerId = oneSignalPlayerId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getOneSignalPlayerId() {
        return oneSignalPlayerId;
    }

    public void setOneSignalPlayerId(String oneSignalPlayerId) {
        this.oneSignalPlayerId = oneSignalPlayerId;
    }
}
