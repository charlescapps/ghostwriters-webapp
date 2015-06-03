package net.capps.word.rest.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by charlescapps on 6/2/15.
 */
public class WordModel implements Comparable<WordModel> {
    private String definition;
    private String word;
    private Boolean played;

    public WordModel() {
    }

    public WordModel(String definition, String word, Boolean played) {
        this.definition = definition;
        this.word = word;
        this.played = played;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Boolean getPlayed() {
        return played;
    }

    public void setPlayed(Boolean played) {
        this.played = played;
    }

    @JsonIgnore
    @Override
    public int compareTo(WordModel o) {
        return word.compareTo(o.word);
    }
}
