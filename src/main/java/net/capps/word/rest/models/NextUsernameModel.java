package net.capps.word.rest.models;

/**
 * Created by charlescapps on 3/15/15.
 */
public class NextUsernameModel {
    private String nextUsername;

    public NextUsernameModel(String nextUsername) {
        this.nextUsername = nextUsername;
    }

    public String getNextUsername() {
        return nextUsername;
    }

    public void setNextUsername(String nextUsername) {
        this.nextUsername = nextUsername;
    }
}
