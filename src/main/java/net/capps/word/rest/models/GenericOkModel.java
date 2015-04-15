package net.capps.word.rest.models;

/**
 * Created by charlescapps on 4/14/15.
 */
public class GenericOkModel {
    private String message;

    public GenericOkModel() {

    }

    public GenericOkModel(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
