package net.capps.word.rest.models;

import java.util.List;

/**
 * Created by charlescapps on 2/1/15.
 */
public class ListModel<T> {

    private List<T> list;

    public ListModel() {

    }

    public ListModel(List<T> list) {
        this.list = list;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
