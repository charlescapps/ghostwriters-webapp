package net.capps.word.game.dict;

/**
 * Created by charlescapps on 3/14/15.
 */
public enum DictType {

    ENGLISH_WORDS("English dictionary", "main_dict.txt"),
    BANNED("Banned words", "banned.txt"),
    ADJECTIVES("Adjectives", "adjectives.txt"),
    NOUNS("Nouns", "nouns.txt"),
    POE("Edgar Allen Poe", "poe_dict.txt"),
    LOVECRAFT("H.P. Lovecraft", "lovecraft_dict.txt"),
    MYTHOS("Cthulhu Mythos", "mythos_dict.txt");

    private static final String PACKAGE = "net/capps/word/dict/";

    private DictType(String description, String filename) {
        this.description = description;
        this.filename = filename;
    }

    private final String description;
    private final String filename;

    public String getResourcePath() {
        return PACKAGE + filename;
    }

    public DictionarySet getDictionarySet() {
        switch (this) {
            case ENGLISH_WORDS: return Dictionaries.getEnglishDictSet();
            case POE: return Dictionaries.getPoeDictSet();
            case LOVECRAFT: return Dictionaries.getLovecraftDictSet();
            case MYTHOS: return Dictionaries.getMythosDictSet();
        }
        throw new IllegalArgumentException("Can only get dictionary set for special dictionaries.");
    }

    public DictionaryTrie getDictionaryTrie() {
        switch (this) {
            case ENGLISH_WORDS: return Dictionaries.getEnglishDictTrie();
            case POE: return Dictionaries.getPoeDictTrie();
            case LOVECRAFT: return Dictionaries.getLovecraftDictTrie();
            case MYTHOS: return Dictionaries.getMythosDictTrie();
        }
        throw new IllegalArgumentException("Can only get dictionary trie for special dictionaries.");
    }

    public DictionaryWordSets getDictionaryWordSets() {
        switch (this) {
            case ENGLISH_WORDS: return Dictionaries.getEnglishWordSets();
            case POE: return Dictionaries.getPoeWordSets();
            case LOVECRAFT: return Dictionaries.getLovecraftWordSets();
            case MYTHOS: return Dictionaries.getMythosWordSets();
        }
        throw new IllegalArgumentException("Can only get dictionary trie for special dictionaries.");
    }
}
