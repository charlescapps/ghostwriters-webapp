package net.capps.word.rest.models;

import net.capps.word.game.dict.SpecialDict;

import java.util.List;

/**
 * Created by charlescapps on 6/3/15.
 */
public class DictionaryModel {
    private SpecialDict specialDict;
    private List<WordModel> words;

    public DictionaryModel() {

    }

    public DictionaryModel(SpecialDict specialDict, List<WordModel> words) {
        this.specialDict = specialDict;
        this.words = words;
    }

    public SpecialDict getSpecialDict() {
        return specialDict;
    }

    public void setSpecialDict(SpecialDict specialDict) {
        this.specialDict = specialDict;
    }

    public List<WordModel> getWords() {
        return words;
    }

    public void setWords(List<WordModel> words) {
        this.words = words;
    }
}
