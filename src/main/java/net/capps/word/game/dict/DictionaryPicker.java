package net.capps.word.game.dict;

import net.capps.word.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
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
    private final Map<Integer, List<String>> wordsByLen = new HashMap<>(12); // 12 possible word lengths, 2-13
    private int longestWord;

    // ---------------- Public ----------------
    /**
     * Load in dictionary from a file.
     *
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public void loadDictionary(Set<String> validDictionary) throws IOException {
        if (!wordsByLen.isEmpty()) {
            throw new IllegalStateException("Cannot load DictionaryWordPicker twice!");
        }

        storeWordsByLength(validDictionary);

        LOG.info("SUCCESS - loaded dictionary into DictionaryWordPicker.");
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
                wordsByLen.put(len, new ArrayList<String>());
            }
            wordsByLen.get(len).add(word);
        }

        // Compute the longest word
        for (int len: wordsByLen.keySet()) {
            longestWord = Math.max(len, longestWord);
        }
    }
}
