package net.capps.word.rest.models;

import java.util.List;

/**
 * Created by charlescapps on 2/1/15.
 */
public class GameListModel {

    private List<GameModel> list;

    public GameListModel() {

    }

    public GameListModel(List<GameModel> list) {
        this.list = list;
    }

    public List<GameModel> getList() {
        return list;
    }

    public void setList(List<GameModel> list) {
        this.list = list;
    }
}
