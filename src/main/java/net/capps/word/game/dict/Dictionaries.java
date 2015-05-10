package net.capps.word.game.dict;

import com.google.common.base.Optional;
import net.capps.word.game.common.BoardSize;

import java.io.IOException;

/**
 * Created by charlescapps on 3/14/15.
 */
public class Dictionaries {
    private static final int MIN_WORD_LEN = 2;
    private static final int MAX_WORD_LEN = BoardSize.VENTI.getN();

    private static final DictionarySet BANNED_SET = new DictionarySet();

    private static final DictionarySet ENGLISH_DICT_SET = new DictionarySet();
    private static final DictionaryTrie ENGLISH_DICT_TRIE = new DictionaryTrie();
    private static final DictionaryWordSets ENGLISH_WORD_SETS = new DictionaryWordSets();

    private static final DictionarySet VICTORIAN_DICT_SET = new DictionarySet();
    private static final DictionaryTrie VICTORIAN_DICT_TRIE = new DictionaryTrie();
    private static final DictionaryWordSets VICTORIAN_WORD_SETS = new DictionaryWordSets();

    private static final DictionarySet LOVECRAFT_DICT_SET = new DictionarySet();
    private static final DictionaryTrie LOVECRAFT_DICT_TRIE = new DictionaryTrie();
    private static final DictionaryWordSets LOVECRAFT_WORD_SETS = new DictionaryWordSets();

    private static final DictionarySet ADJECTIVES_SET = new DictionarySet();
    private static final DictionaryPicker ADJECTIVES_PICKER = new DictionaryPicker();

    private static final DictionarySet NOUNS_SET = new DictionarySet();
    private static final DictionaryPicker NOUNS_PICKER = new DictionaryPicker();

    public static void initializeAllDictionaries() throws IOException {
        BANNED_SET.loadDictionary(DictType.BANNED.getResourcePath(), MIN_WORD_LEN, MAX_WORD_LEN, Optional.<DictionarySet>absent());

        ENGLISH_DICT_SET.loadDictionary(DictType.ENGLISH_WORDS.getResourcePath(), MIN_WORD_LEN, MAX_WORD_LEN, Optional.of(BANNED_SET));
        ENGLISH_DICT_TRIE.loadDictionary(ENGLISH_DICT_SET.getWords());
        ENGLISH_WORD_SETS.loadDictionarySets(ENGLISH_DICT_SET.getWords());

        VICTORIAN_DICT_SET.loadDictionary(DictType.VICTORIAN.getResourcePath(), MIN_WORD_LEN, MAX_WORD_LEN, Optional.of(BANNED_SET));
        VICTORIAN_DICT_TRIE.loadDictionary(VICTORIAN_DICT_SET.getWords());
        VICTORIAN_WORD_SETS.loadDictionarySets(VICTORIAN_DICT_SET.getWords());

        LOVECRAFT_DICT_SET.loadDictionary(DictType.LOVECRAFT.getResourcePath(), MIN_WORD_LEN, MAX_WORD_LEN, Optional.of(BANNED_SET));
        LOVECRAFT_DICT_TRIE.loadDictionary(LOVECRAFT_DICT_SET.getWords());
        LOVECRAFT_WORD_SETS.loadDictionarySets(LOVECRAFT_DICT_SET.getWords());

        ADJECTIVES_SET.loadDictionary(DictType.ADJECTIVES.getResourcePath(), MIN_WORD_LEN, MAX_WORD_LEN, Optional.of(BANNED_SET));
        ADJECTIVES_PICKER.loadDictionary(ADJECTIVES_SET.getWords());

        NOUNS_SET.loadDictionary(DictType.NOUNS.getResourcePath(), MIN_WORD_LEN, MAX_WORD_LEN, Optional.of(BANNED_SET));
        NOUNS_PICKER.loadDictionary(NOUNS_SET.getWords());
    }

    public static DictionarySet getEnglishDictSet() {
        return ENGLISH_DICT_SET;
    }

    public static DictionaryTrie getEnglishDictTrie() {
        return ENGLISH_DICT_TRIE;
    }

    public static DictionaryWordSets getEnglishWordSets() {
        return ENGLISH_WORD_SETS;
    }

    public static DictionarySet getLovecraftDictSet() {
        return LOVECRAFT_DICT_SET;
    }

    public static DictionaryTrie getLovecraftDictTrie() {
        return LOVECRAFT_DICT_TRIE;
    }

    public static DictionaryWordSets getLovecraftWordSets() {
        return LOVECRAFT_WORD_SETS;
    }

    public static DictionarySet getVictorianDictSet() {
        return VICTORIAN_DICT_SET;
    }

    public static DictionaryTrie getVictorianDictTrie() {
        return VICTORIAN_DICT_TRIE;
    }

    public static DictionaryWordSets getVictorianWordSets() {
        return VICTORIAN_WORD_SETS;
    }

    public static DictionarySet getAdjectivesSet() {
        return ADJECTIVES_SET;
    }

    public static DictionaryPicker getAdjectivesPicker() {
        return ADJECTIVES_PICKER;
    }

    public static DictionarySet getNounsSet() {
        return NOUNS_SET;
    }

    public static DictionaryPicker getNounsPicker() {
        return NOUNS_PICKER;
    }
}
