package net.capps.word.rest.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by charlescapps on 12/27/14.
 */
@XmlRootElement
public class ErrorModel {
    @JsonProperty
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
