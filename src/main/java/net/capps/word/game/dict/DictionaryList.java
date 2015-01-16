package net.capps.word.game.dict;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Created by charlescapps on 1/15/15.
 */
public class DictionaryList {
    // ---------------- Static ----------------
    private static final DictionaryList INSTANCE = new DictionaryList();
    private static final Random RANDOM = new Random();
    private static final Logger LOG = LoggerFactory.getLogger(DictionaryList.class);

    public static DictionaryList getInstance() {
        return INSTANCE;
    }

    // ---------------- Constructor -----------
    private DictionaryList() { }

    // ---------------- Private fields ---------------
    private ImmutableList<String> words;
    private final Map<Integer, List<String>> wordsByLen = Maps.newHashMap();
    private int longestWord;
    private List<Integer> lengths;

    // ---------------- Public ----------------
    /**
     * Load in dictionary from a file.
     *
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public void loadDictionary(Set<String> validDictionary) throws IOException {
        if (words != null) {
            throw new IllegalStateException(
                    String.format("Cannot load dictionary twice! Dictionary already has %d entries!", words.size()));
        }

        words = ImmutableList.<String>builder().addAll(validDictionary).build();
        storeWordsByLength(validDictionary);

        LOG.info("SUCCESS - loaded dictionary into array and by length.");
    }

    public String getUniformlyRandomWord() {
        int index = RANDOM.nextInt(words.size());
        return words.get(index);
    }

    /**
     * Get a random word by
     * 1) Choosing a random length from all possible lengths of words.
     * 2) Choosing a random word from words of that length.
     */
    public String getRandomWordEqualProbabilityByLength() {
        return getRandomWordEqualProbabilityByLength(Integer.MAX_VALUE);
    }

    /**
     * Get a random word by
     * 1) Choosing a random length <= maxLength
     * 2) Choosing a random word from words of that length.
     * @param maxLength max length for the randomly chosen word.
     */
    public String getRandomWordEqualProbabilityByLength(int maxLength) {
        if (maxLength < 2) {
            throw new IllegalArgumentException("The max word length for a random word cannot be less than 2");
        }

        int i = Math.min(maxLength - 1, lengths.size() - 1);

        while (lengths.get(i) > maxLength) {
            --i;
        }

        int index = RANDOM.nextInt(i + 1);
        int chosenLen = lengths.get(index);
        
        List<String> wordsOfLen = wordsByLen.get(chosenLen);

        index = RANDOM.nextInt(wordsOfLen.size());
        return words.get(index);
    }

    // ------------- Private helpers -----------
    private void storeWordsByLength(Set<String> validDictionary) {
        // Store the words by length
        for (String word: validDictionary) {
            int len = word.length();
            if (!wordsByLen.containsKey(len)) {
                wordsByLen.put(len, Lists.<String>newArrayList());
            }
            wordsByLen.get(len).add(word);
        }

        // Compute the longest word
        for (int len: wordsByLen.keySet()) {
            longestWord = Math.max(len, longestWord);
        }

        // Store the array of different lengths
        lengths = Lists.newArrayList(wordsByLen.keySet());
        Collections.sort(lengths);
    }
}
