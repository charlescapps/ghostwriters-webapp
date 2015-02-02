package net.capps.word.rest.models;

import java.util.List;

/**
 * Created by charlescapps on 2/1/15.
 */
public class UserListModel {

    private List<UserModel> users;

    public UserListModel() {

    }

    public UserListModel(List<UserModel> users) {
        this.users = users;
    }

    public List<UserModel> getUsers() {
        return users;
    }

    public void setUsers(List<UserModel> users) {
        this.users = users;
    }
}
