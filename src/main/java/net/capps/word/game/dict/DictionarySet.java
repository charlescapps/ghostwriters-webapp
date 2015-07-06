package net.capps.word.game.dict;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URL;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by charlescapps on 1/15/15.
 */
public class DictionarySet {
    // ---------------- Static ----------------
    private static final Pattern WORD_PATTERN = Pattern.compile("[a-zA-Z]+");
    private static final Pattern WORD_WITH_DEFINITION_PATTERN = Pattern.compile("([a-zA-Z]+) +\"((?:[^\"]|\\\\\")+)\" *");
    private static final Logger LOG = LoggerFactory.getLogger(DictionarySet.class);

    // ---------------- Constructor -----------
    DictionarySet() { } // Must call loadDictionary() after instantiation

    // ---------------- Private ---------------
    private ImmutableSet<String> wordSet;
    private ImmutableList<String> wordList;
    private ImmutableMap<String, Integer> wordToIndex;
    private ImmutableMap<String, String> definitions;

    // ---------------- Public ----------------
    /**
     * Load in dictionary from a file.
     *
     * @param resourceFile path to a resource text file containing a dictionary.
     * @param bannedWords
     * @param storeList
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void loadDictionary(final String resourceFile, final int minWordLength, final int maxWordLength, Optional<DictionarySet> bannedWords, boolean storeList) throws IOException {
        if (wordSet != null) {
            throw new IllegalStateException(
                    String.format("Cannot load dictionary twice! Dictionary already has %d entries!", wordSet.size()));
        }

        URL resource = getClass().getClassLoader().getResource(resourceFile);
        if (resource == null || Strings.isNullOrEmpty(resource.getPath())) {
            throw new IllegalArgumentException("Invalid dictionary file: " + resourceFile);
        }
        File file = new File(resource.getPath());

        LOG.info("***** Starting to load dictionary set from file: {} *****", file.getName());
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {

            ImmutableSet.Builder<String> setBuilder = ImmutableSet.builder();
            ImmutableList.Builder<String> listBuilder = ImmutableList.builder();
            ImmutableMap.Builder<String, Integer> wordToIndexBuilder = ImmutableMap.builder();
            ImmutableMap.Builder<String, String> definitionsBuilder = ImmutableMap.builder();

            int wordIndex = 0;
            String line;
            while ((line = br.readLine()) != null) {
                String wordAndDefinition = line.trim();
                String word, definition = null;

                Matcher wordAndDefinitionMatcher = WORD_WITH_DEFINITION_PATTERN.matcher(wordAndDefinition);

                if (wordAndDefinitionMatcher.matches()) {
                    word = wordAndDefinitionMatcher.group(1);
                    definition = wordAndDefinitionMatcher.group(2);
                } else {
                    Matcher wordMatcher = WORD_PATTERN.matcher(wordAndDefinition);
                    if (!wordMatcher.matches()) {
                        LOG.error("Error - invalid line found in dictionary: '{}'", wordAndDefinition);
                        continue;
                    }
                    word = wordAndDefinition;
                }
                word = word.toUpperCase();

                if (word.length() > maxWordLength || word.length() < minWordLength) {
                    LOG.trace("Ignoring word longer than {} or shorter than {}: {}", maxWordLength, minWordLength, wordAndDefinition);
                    continue;
                }

                if (bannedWords.isPresent() && bannedWords.get().contains(word)) {
                    LOG.trace("Not including banned word '{}'", wordAndDefinition);
                    continue;
                }
                word = word.intern(); // Avoid duplicate strings being stored elsewhere in the JVM
                setBuilder.add(word);
                if (storeList) {
                    listBuilder.add(word);
                    wordToIndexBuilder.put(word, wordIndex);
                    ++wordIndex;
                }
                if (definition != null) {
                    String unescapedDef = definition.replace("\\\"", "\"");
                    definitionsBuilder.put(word, unescapedDef);
                }
            }
            wordSet = setBuilder.build();
            wordList = listBuilder.build();
            wordToIndex = wordToIndexBuilder.build();
            definitions = definitionsBuilder.build();
        }
        LOG.info("SUCCESS - loaded {} words, and {} definitions from file {}!", wordSet.size(), definitions.size(), file.getName());
    }

    public boolean contains(String word) {
        return wordSet.contains(word);
    }

    public ImmutableSet<String> getWordSet() {
        return wordSet;
    }

    public ImmutableList<String> getWordList() {
        return wordList;
    }

    public ImmutableMap<String, String> getDefinitions() {
        return definitions;
    }

    @Nullable
    public Integer getWordIndex(String word) {
        return wordToIndex.get(word);
    }
}
