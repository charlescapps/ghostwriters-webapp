package net.capps.word.game.dict;

import net.capps.word.game.common.BoardSize;

import java.io.IOException;
import java.util.Optional;

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

    private static final DictionarySet POE_DICT_SET = new DictionarySet();
    private static final DictionaryTrie POE_DICT_TRIE = new DictionaryTrie();
    private static final DictionaryWordSets POE_WORD_SETS = new DictionaryWordSets();

    private static final DictionarySet LOVECRAFT_DICT_SET = new DictionarySet();
    private static final DictionaryTrie LOVECRAFT_DICT_TRIE = new DictionaryTrie();
    private static final DictionaryWordSets LOVECRAFT_WORD_SETS = new DictionaryWordSets();

    private static final DictionarySet MYTHOS_DICT_SET = new DictionarySet();
    private static final DictionaryTrie MYTHOS_DICT_TRIE = new DictionaryTrie();
    private static final DictionaryWordSets MYTHOS_WORD_SETS = new DictionaryWordSets();

    private static final DictionarySet ADJECTIVES_SET = new DictionarySet();
    private static final DictionaryPicker ADJECTIVES_PICKER = new DictionaryPicker();

    private static final DictionarySet NOUNS_SET = new DictionarySet();
    private static final DictionaryPicker NOUNS_PICKER = new DictionaryPicker();

    public static void initializeAllDictionaries() throws IOException {
        BANNED_SET.loadDictionary(DictType.BANNED.getResourcePath(), MIN_WORD_LEN, MAX_WORD_LEN, Optional.empty(), false);

        ENGLISH_DICT_SET.loadDictionary(DictType.ENGLISH_WORDS.getResourcePath(), MIN_WORD_LEN, MAX_WORD_LEN, Optional.of(BANNED_SET), false);
        ENGLISH_DICT_TRIE.loadDictionary(ENGLISH_DICT_SET.getWordSet());
        ENGLISH_WORD_SETS.loadDictionarySets(ENGLISH_DICT_SET.getWordSet());

        POE_DICT_SET.loadDictionary(DictType.POE.getResourcePath(), MIN_WORD_LEN, MAX_WORD_LEN, Optional.of(BANNED_SET), true);
        POE_DICT_TRIE.loadDictionary(POE_DICT_SET.getWordSet());
        POE_WORD_SETS.loadDictionarySets(POE_DICT_SET.getWordSet());

        LOVECRAFT_DICT_SET.loadDictionary(DictType.LOVECRAFT.getResourcePath(), MIN_WORD_LEN, MAX_WORD_LEN, Optional.of(BANNED_SET), true);
        LOVECRAFT_DICT_TRIE.loadDictionary(LOVECRAFT_DICT_SET.getWordSet());
        LOVECRAFT_WORD_SETS.loadDictionarySets(LOVECRAFT_DICT_SET.getWordSet());

        MYTHOS_DICT_SET.loadDictionary(DictType.MYTHOS.getResourcePath(), MIN_WORD_LEN, MAX_WORD_LEN, Optional.of(BANNED_SET), true);
        MYTHOS_DICT_TRIE.loadDictionary(MYTHOS_DICT_SET.getWordSet());
        MYTHOS_WORD_SETS.loadDictionarySets(MYTHOS_DICT_SET.getWordSet());

        ADJECTIVES_SET.loadDictionary(DictType.ADJECTIVES.getResourcePath(), MIN_WORD_LEN, MAX_WORD_LEN, Optional.of(BANNED_SET), false);
        ADJECTIVES_PICKER.loadDictionary(ADJECTIVES_SET.getWordSet());

        NOUNS_SET.loadDictionary(DictType.NOUNS.getResourcePath(), MIN_WORD_LEN, MAX_WORD_LEN, Optional.of(BANNED_SET), false);
        NOUNS_PICKER.loadDictionary(NOUNS_SET.getWordSet());
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

    public static DictionarySet getMythosDictSet() {
        return MYTHOS_DICT_SET;
    }

    public static DictionaryTrie getMythosDictTrie() {
        return MYTHOS_DICT_TRIE;
    }

    public static DictionaryWordSets getMythosWordSets() {
        return MYTHOS_WORD_SETS;
    }

    public static DictionarySet getPoeDictSet() {
        return POE_DICT_SET;
    }

    public static DictionaryTrie getPoeDictTrie() {
        return POE_DICT_TRIE;
    }

    public static DictionaryWordSets getPoeWordSets() {
        return POE_WORD_SETS;
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
