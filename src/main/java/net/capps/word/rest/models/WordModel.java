package net.capps.word.rest.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by charlescapps on 6/2/15.
 */
public class WordModel implements Comparable<WordModel> {
    private String d;
    private String w;
    private Boolean p;

    public WordModel() {
    }

    public WordModel(String d, String w, Boolean p) {
        this.d = d;
        this.w = w;
        this.p = p;
    }

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }

    public String getW() {
        return w;
    }

    public void setW(String w) {
        this.w = w;
    }

    public Boolean getP() {
        return p;
    }

    public void setP(Boolean p) {
        this.p = p;
    }

    @JsonIgnore
    @Override
    public int compareTo(WordModel o) {
        return w.compareTo(o.w);
    }
}
