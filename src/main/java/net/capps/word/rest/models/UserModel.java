package net.capps.word.rest.models;

import net.capps.word.db.dao.UserHashInfo;
import net.capps.word.game.ranking.EloRankingComputer;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Objects;

/**
 * Created by charlescapps on 12/26/14.
 */
@XmlRootElement
public class UserModel {
    private Integer id;
    private String username;
    private String email;
    private Long dateJoined;
    private String deviceId;
    private UserHashInfo userHashInfo; // Not serialized
    private Boolean systemUser; // Not serialized
    private Integer dbRating; // elo rating * 1000.

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

    @XmlTransient
    public Boolean getSystemUser() {
        return systemUser;
    }

    public void setSystemUser(Boolean systemUser) {
        this.systemUser = systemUser;
    }

    @XmlTransient
    public Integer getDbRating() {
        return dbRating;
    }

    public void setDbRating(Integer dbRating) {
        this.dbRating = dbRating;
    }

    public Integer getRating() {
        return (int) ( dbRating / EloRankingComputer.DATABASE_FACTOR );
    }

    @XmlTransient
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
}
