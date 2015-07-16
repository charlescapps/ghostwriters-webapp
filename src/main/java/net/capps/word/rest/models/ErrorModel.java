package net.capps.word.rest.models;

/**
 * Created by charlescapps on 12/27/14.
 */
public class ErrorModel {
    private String errorMessage;

    public ErrorModel() {

    }

    public ErrorModel(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
