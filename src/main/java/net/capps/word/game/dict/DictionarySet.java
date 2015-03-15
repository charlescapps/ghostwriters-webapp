package net.capps.word.game.dict;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by charlescapps on 1/15/15.
 */
public class DictionarySet {
    // ---------------- Static ----------------
    private static final Pattern VALID_WORD_PATTERN = Pattern.compile("[A-Z]+");
    private static final Logger LOG = LoggerFactory.getLogger(DictionarySet.class);

    // ---------------- Constructor -----------
    DictionarySet() { } // Must call loadDictionary() after instantiation

    // ---------------- Private ---------------
    private ImmutableSet<String> words;

    // ---------------- Public ----------------
    /**
     * Load in dictionary from a file.
     *
     * @param resourceFile path to a resource text file containing a dictionary.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void loadDictionary(final String resourceFile, final int minWordLength, final int maxWordLength) throws IOException {
        if (words != null) {
            throw new IllegalStateException(
                    String.format("Cannot load dictionary twice! Dictionary already has %d entries!", words.size()));
        }

        URL resource = getClass().getClassLoader().getResource(resourceFile);
        if (resource == null || Strings.isNullOrEmpty(resource.getPath())) {
            throw new IllegalArgumentException("Invalid dictionary file: " + resourceFile);
        }
        File file = new File(resource.getPath());

        LOG.info("***** Starting to load dictionary set from file: {} *****", file.getPath());
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {

            ImmutableSet.Builder<String> builder = ImmutableSet.builder();
            String line;
            while ((line = br.readLine()) != null) {
                String word = line.trim().toUpperCase();
                Matcher m = VALID_WORD_PATTERN.matcher(word);
                if (word.length() > maxWordLength || word.length() < minWordLength) {
                    LOG.info("Ignoring word longer than {} or shorter than {}: {}", maxWordLength, minWordLength, word);
                    continue;
                }
                if (!m.matches()) {
                    LOG.error("Error - invalid word found in dictionary: '{}'", word);
                    continue;
                }
                builder.add(word);
            }
            words = builder.build();
        }
        LOG.info("SUCCESS - loaded {} words from file {}!", words.size(), file.getPath());
    }

    public boolean contains(String word) {
        return words.contains(word);
    }

    public ImmutableSet<String> getWords() {
        return words;
    }
}
