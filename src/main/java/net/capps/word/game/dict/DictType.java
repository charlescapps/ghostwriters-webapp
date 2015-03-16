package net.capps.word.game.dict;

import java.io.InputStream;

/**
 * Created by charlescapps on 3/14/15.
 */
public enum DictType {

    ALL_WORDS("English dictionary", "scowl.words.80.lowercase"),
    ADJECTIVES("Adjectives", "adjectives.txt"),
    NOUNS("Nouns", "nouns.txt"),
    VICTORIAN("Charles Dickens", "dickens_dict.filtered.txt"),
    HORROR("H.P. Lovecraft", "hp_lovecraft_dict.txt");

    private static final String PACKAGE = "net/capps/word/dict/";

    private DictType(String description, String filename) {
        this.description = description;
        this.filename = filename;
    }

    private final String description;
    private final String filename;

    public String getDescription() {
        return description;
    }

    public String getFilename() {
        return filename;
    }

    public String getResourcePath() {
        return PACKAGE + filename;
    }

    public InputStream openFile() {
        return getClass().getResourceAsStream(PACKAGE + filename);
    }
}
