package net.capps.word.models;

import java.util.Objects;

/**
 * Created by charlescapps on 12/26/14.
 */
public class WordUserModel {
    private Integer id;
    private String username;
    private String email;

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
