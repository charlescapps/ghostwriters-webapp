package net.capps.word.rest.models;

/**
 * Created by charlescapps on 3/15/15.
 */
public class NextUsernameModel {
    private String nextUsername;
    private Boolean required;

    public NextUsernameModel() {}

    public NextUsernameModel(String nextUsername, boolean required) {
        this.nextUsername = nextUsername;
        this.required = required;
    }

    public String getNextUsername() {
        return nextUsername;
    }

    public void setNextUsername(String nextUsername) {
        this.nextUsername = nextUsername;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }
}
