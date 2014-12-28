package net.capps.word.models;

import java.util.Objects;

/**
 * Created by charlescapps on 12/26/14.
 */
public class WordUserModel {
    private Integer id;
    private String username;
    private String email;
    private String password; // Only present when creating a new user

    public WordUserModel() {

    }

    public WordUserModel(Integer id, String username, String email, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof WordUserModel)) {
            return false;
        }
        WordUserModel other = (WordUserModel)o;
        return Objects.equals(id, other.id) &&
               Objects.equals(username, other.username);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id) ^ Objects.hashCode(username);
    }
}
