package net.capps.word.game.dict;

import java.io.InputStream;

/**
 * Created by charlescapps on 3/14/15.
 */
public enum DictType {

    ALL_WORDS("English dictionary", "main_dict.txt"),
    BANNED("Banned words", "banned.txt"),
    ADJECTIVES("Adjectives", "adjectives.txt"),
    NOUNS("Nouns", "nouns.txt"),
    VICTORIAN("Victorian Era", "dickens_dict.filtered.txt"),
    LOVECRAFT("H.P. Lovecraft", "hp_lovecraft_dict.txt");

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

    public DictionarySet getDictionarySet() {
        switch (this) {
            case VICTORIAN: return Dictionaries.getVictorianWordsSet();
            case LOVECRAFT: return Dictionaries.getLovecraftWordsSet();
        }
        throw new IllegalArgumentException("Can only get dictionary set for special dictionaries.");
    }

    public DictionaryTrie getDictionaryTrie() {
        switch (this) {
            case VICTORIAN: return Dictionaries.getVictorianWordsTrie();
            case LOVECRAFT: return Dictionaries.getLovecraftWordsTrie();
        }
        throw new IllegalArgumentException("Can only get dictionary trie for special dictionaries.");
    }
}
