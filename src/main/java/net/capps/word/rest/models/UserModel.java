package net.capps.word.rest.models;

import net.capps.word.db.dao.UserHashInfo;

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
    private String password; // Only present when creating a new user
    private UserHashInfo userHashInfo;

    public UserModel() {

    }

    public UserModel(Integer id, String username, String email, String password, UserHashInfo userHashInfo) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.userHashInfo = userHashInfo;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
