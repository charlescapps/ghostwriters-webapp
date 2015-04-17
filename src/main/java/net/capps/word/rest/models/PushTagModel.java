package net.capps.word.rest.models;

/**
 * Created by charlescapps on 4/9/15.
 */
public class PushTagModel {
    private String key;
    private String value;
    private String relation;

    public PushTagModel() {

    }

    public PushTagModel(String key, String value, String relation) {
        this.key = key;
        this. value = value;
        this.relation = relation;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }
}
