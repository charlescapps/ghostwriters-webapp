package net.capps.word.models;

/**
 * Created by charlescapps on 12/27/14.
 */
public class ErrorModel {
    private String errorMessage;

    public ErrorModel(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
