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

    private static final DictionarySet ALL_WORDS_SET = new DictionarySet();
    private static final DictionaryTrie ALL_WORDS_TRIE = new DictionaryTrie();
    private static final DictionaryPicker ALL_WORDS_PICKER = new DictionaryPicker();

    private static final DictionarySet VICTORIAN_WORDS_SET = new DictionarySet();
    private static final DictionaryTrie VICTORIAN_WORDS_TRIE = new DictionaryTrie();

    private static final DictionarySet HORROR_WORDS_SET = new DictionarySet();
    private static final DictionaryTrie HORROR_WORDS_TRIE = new DictionaryTrie();

    private static final DictionarySet ADJECTIVES_SET = new DictionarySet();
    private static final DictionaryPicker ADJECTIVES_PICKER = new DictionaryPicker();

    private static final DictionarySet NOUNS_SET = new DictionarySet();
    private static final DictionaryPicker NOUNS_PICKER = new DictionaryPicker();

    public static void initializeAllDictionaries() throws IOException {
        BANNED_SET.loadDictionary(DictType.BANNED.getResourcePath(), MIN_WORD_LEN, MAX_WORD_LEN, Optional.<DictionarySet>absent());

        ALL_WORDS_SET.loadDictionary(DictType.ALL_WORDS.getResourcePath(), MIN_WORD_LEN, MAX_WORD_LEN, Optional.of(BANNED_SET));
        ALL_WORDS_TRIE.loadDictionary(ALL_WORDS_SET.getWords());
        ALL_WORDS_PICKER.loadDictionary(ALL_WORDS_SET.getWords());

        VICTORIAN_WORDS_SET.loadDictionary(DictType.VICTORIAN.getResourcePath(), MIN_WORD_LEN, MAX_WORD_LEN, Optional.of(BANNED_SET));
        VICTORIAN_WORDS_TRIE.loadDictionary(VICTORIAN_WORDS_SET.getWords());

        HORROR_WORDS_SET.loadDictionary(DictType.HORROR.getResourcePath(), MIN_WORD_LEN, MAX_WORD_LEN, Optional.of(BANNED_SET));
        HORROR_WORDS_TRIE.loadDictionary(HORROR_WORDS_SET.getWords());

        ADJECTIVES_SET.loadDictionary(DictType.ADJECTIVES.getResourcePath(), MIN_WORD_LEN, MAX_WORD_LEN, Optional.of(BANNED_SET));
        ADJECTIVES_PICKER.loadDictionary(ADJECTIVES_SET.getWords());

        NOUNS_SET.loadDictionary(DictType.NOUNS.getResourcePath(), MIN_WORD_LEN, MAX_WORD_LEN, Optional.of(BANNED_SET));
        NOUNS_PICKER.loadDictionary(NOUNS_SET.getWords());
    }

    public static DictionarySet getAllWordsSet() {
        return ALL_WORDS_SET;
    }

    public static DictionaryTrie getAllWordsTrie() {
        return ALL_WORDS_TRIE;
    }

    public static DictionaryPicker getAllWordsPicker() {
        return ALL_WORDS_PICKER;
    }

    public static DictionarySet getHorrorWordsSet() {
        return HORROR_WORDS_SET;
    }

    public static DictionaryTrie getHorrorWordsTrie() {
        return HORROR_WORDS_TRIE;
    }

    public static DictionarySet getVictorianWordsSet() {
        return VICTORIAN_WORDS_SET;
    }

    public static DictionaryTrie getVictorianWordsTrie() {
        return VICTORIAN_WORDS_TRIE;
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
