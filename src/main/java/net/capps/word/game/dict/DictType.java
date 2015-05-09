package net.capps.word.game.dict;

import java.io.InputStream;

/**
 * Created by charlescapps on 3/14/15.
 */
public enum DictType {

    ENGLISH_WORDS("English dictionary", "main_dict.txt", false),
    BANNED("Banned words", "banned.txt", false),
    ADJECTIVES("Adjectives", "adjectives.txt", false),
    NOUNS("Nouns", "nouns.txt", false),
    VICTORIAN("Victorian Era", "dickens_dict.filtered.txt", true),
    LOVECRAFT("H.P. Lovecraft", "hp_lovecraft_dict.txt", true);

    private static final String PACKAGE = "net/capps/word/dict/";

    private DictType(String description, String filename, boolean isSpecialDict) {
        this.description = description;
        this.filename = filename;
        this.isSpecialDict = isSpecialDict;
    }

    private final String description;
    private final String filename;
    private final boolean isSpecialDict;

    public String getDescription() {
        return description;
    }

    public String getFilename() {
        return filename;
    }

    public boolean isSpecialDict() {
        return isSpecialDict;
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
