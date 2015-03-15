package net.capps.word.game.dict;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.capps.word.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by charlescapps on 1/15/15.
 */
public class DictionaryPicker {
    // ---------------- Static ----------------
    private static final Logger LOG = LoggerFactory.getLogger(DictionaryPicker.class);


    // ---------------- Constructor -----------
    DictionaryPicker() { }

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
            throw new IllegalStateException("Cannot load DictionaryWordPicker twice!");
        }

        words = ImmutableList.<String>builder().addAll(validDictionary).build();
        storeWordsByLength(validDictionary);

        LOG.info("SUCCESS - loaded dictionary into DictionaryWordPicker.");
    }

    public String getUniformlyRandomWord() {
        int index = ThreadLocalRandom.current().nextInt(words.size());
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

        return getRandomWordEqualProbabilityByLength(2, maxLength);
    }

    public String getRandomWordEqualProbabilityByLength(int minLen, int maxLen) {
        if (maxLen < minLen) {
            throw new IllegalArgumentException("The max word length for a random word cannot be less than the min length");
        }

        int chosenLength = RandomUtil.randomInt(minLen, maxLen);
        List<String> wordsOfLen = wordsByLen.get(chosenLength);

        int index = ThreadLocalRandom.current().nextInt(wordsOfLen.size());
        return wordsOfLen.get(index);
    }

    public String getRandomWordBetweenLengths(int minLen, int maxLen) {
        // Compute total number of such words
        int totalWords = 0;
        for (int i = minLen; i <= maxLen; i++) {
            List<String> words = wordsByLen.get(i);
            if (words != null) {
                totalWords += words.size();
            }
        }

        // Get a random index into the words
        int index = ThreadLocalRandom.current().nextInt(totalWords);
        int len = minLen;
        List<String> wordsForLen = wordsByLen.get(len);
        while (index >= wordsForLen.size()) {
            index -= wordsForLen.size();
            ++len;
            wordsForLen = wordsByLen.get(len);
        }
        return wordsForLen.get(index);
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
