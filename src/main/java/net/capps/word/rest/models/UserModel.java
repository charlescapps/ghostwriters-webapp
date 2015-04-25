package net.capps.word.rest.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import net.capps.word.db.dao.UserHashInfo;

import java.util.Objects;

/**
 * Created by charlescapps on 12/26/14.
 */
public class UserModel {
    private Integer id;
    private String username;
    private String email;
    private Long dateJoined;
    private String deviceId;
    private UserHashInfo userHashInfo; // Not serialized
    private Boolean systemUser; // Not serialized
    private Integer rating; // elo rating * 1000.
    private Integer wins;
    private Integer losses;
    private Integer ties;
    private Integer rank;

    public UserModel() {

    }

    public UserModel(Integer id, String username, String email, String deviceId, UserHashInfo userHashInfo, boolean systemUser) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.deviceId = deviceId;
        this.userHashInfo = userHashInfo;
        this.systemUser = systemUser;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getDateJoined() {
        return dateJoined;
    }

    public void setDateJoined(Long dateJoined) {
        this.dateJoined = dateJoined;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setUserHashInfo(UserHashInfo userHashInfo) {
        this.userHashInfo = userHashInfo;
    }

    @JsonIgnore
    public Boolean getSystemUser() {
        return systemUser;
    }

    public void setSystemUser(Boolean systemUser) {
        this.systemUser = systemUser;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Integer getWins() {
        return wins;
    }

    public void setWins(Integer wins) {
        this.wins = wins;
    }

    public Integer getLosses() {
        return losses;
    }

    public void setLosses(Integer losses) {
        this.losses = losses;
    }

    public Integer getTies() {
        return ties;
    }

    public void setTies(Integer ties) {
        this.ties = ties;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    @JsonIgnore
    public UserHashInfo getUserHashInfo() {
        return userHashInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserModel)) {
            return false;
        }
        UserModel other = (UserModel)o;
        return Objects.equals(id, other.id) &&
               Objects.equals(username, other.username);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id) ^ Objects.hashCode(username);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("username", username)
                .add("rating", rating)
                .add("rank", rank)
                .toString();
    }
}
