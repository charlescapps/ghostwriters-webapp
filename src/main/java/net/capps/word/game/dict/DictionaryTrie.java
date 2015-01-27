package net.capps.word.game.dict;

import net.capps.word.game.dict.iterators.RandomWordIteratorFactory;
import net.capps.word.game.dict.iterators.WordsOfLengthIterator;
import net.capps.word.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;

/**
 * Created by charlescapps on 1/16/15.
 */
public class DictionaryTrie {
    // ---------------- Static ----------------
    private static final DictionaryTrie INSTANCE = new DictionaryTrie();
    private static final Logger LOG = LoggerFactory.getLogger(DictionaryTrie.class);

    public static DictionaryTrie getInstance() {
        return INSTANCE;
    }

    // ---------------- Constructor -----------
    private DictionaryTrie() { }

    // ---------------- Private fields ---------------
    private final TrieNode root = new TrieNode(null, "");

    // ---------------- Public ----------------
    /**
     * Load in dictionary from a file.
     *
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public void loadDictionary(Set<String> validDictionary) throws IOException {
        if (!root.isLeaf()) {
            throw new IllegalStateException("Cannot load DictionaryTrie twice!");
        }

        LOG.info("Starting to load dictionary into Trie...");
        long START = System.currentTimeMillis();
        for (String word: validDictionary) {
            insertWord(word, root);
        }
        long END = System.currentTimeMillis();


        LOG.info(format("SUCCESS - loaded dictionary into DictionaryTrie in %s.", DateUtil.getDurationPretty(END - START)));

        LOG.info("Starting to build the Level maps...");
        START = System.currentTimeMillis();
        buildAllLevelsFromRoot(root);
        END = System.currentTimeMillis();

        LOG.info(format("SUCCESS - created Level Maps in %s.", DateUtil.getDurationPretty(END - START)));

    }

    public boolean contains(String str) {
        return contains(str, root);
    }

    public boolean isPrefix(String str) {
        return isPrefix(str, root);
    }

    public Iterator<String> getWordsWithConstraintsInRandomOrder(List<WordConstraint> constraints, int len) {
        return RandomWordIteratorFactory.create(root, constraints, len);
    }

    public Iterator<String> getWordsOfLengthInRandomOrder(int len) {
        return WordsOfLengthIterator.create(root, len);
    }

    // --------------- Private ---------------
    private static boolean isPrefix(String str, TrieNode node) {
        if (str.isEmpty()) {
            return true;
        }
        char c = str.charAt(0);
        TrieNode child = node.getChild(c);
        if (child == null) {
            return false;
        }
        return isPrefix(str.substring(1), child);
    }

    private static boolean contains(String str, TrieNode node) {
        if (str.isEmpty()) {
            return node.isValidWord();
        }
        char c = str.charAt(0);
        TrieNode child = node.getChild(c);
        if (child == null) {
            return false;
        }
        return contains(str.substring(1), child);
    }

    private static void insertWord(String word, TrieNode TrieNode) {
        if (word.isEmpty()) {
            TrieNode.setValidWord(true);
            return;
        }
        char c = word.charAt(0);
        TrieNode child = TrieNode.addChild(c);
        insertWord(word.substring(1), child);
    }

    private static void buildAllLevelsFromRoot(TrieNode node) {
        node.buildLevels();
        for (TrieNode child: node.getChildren()) {
            buildAllLevelsFromRoot(child);
        }
    }

}
