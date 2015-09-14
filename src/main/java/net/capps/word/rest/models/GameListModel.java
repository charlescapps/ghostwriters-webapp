package net.capps.word.rest.models;

import java.util.List;

/**
 * Created by charlescapps on 2/1/15.
 */
public class GameListModel {

    private List<GameModel> list;
    private Integer nextPage;

    public GameListModel() {

    }

    public GameListModel(List<GameModel> list, Integer nextPage) {
        this.list = list;
        this.nextPage = nextPage;
    }

    public List<GameModel> getList() {
        return list;
    }

    public void setList(List<GameModel> list) {
        this.list = list;
    }

    public Integer getNextPage() {
        return nextPage;
    }

    public void setNextPage(Integer nextPage) {
        this.nextPage = nextPage;
    }
}
