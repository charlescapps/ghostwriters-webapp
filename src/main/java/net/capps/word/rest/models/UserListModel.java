package net.capps.word.rest.models;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by charlescapps on 2/1/15.
 */
@XmlRootElement
public class UserListModel {

    private List<UserModel> list;

    public UserListModel() {

    }

    public UserListModel(List<UserModel> list) {
        this.list = list;
    }

    public List<UserModel> getList() {
        return list;
    }

    public void setList(List<UserModel> list) {
        this.list = list;
    }
}
