package net.capps.word.game.dict;

import net.capps.word.game.common.BoardSize;
import net.capps.word.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

import static java.lang.String.format;

/**
 * Created by charlescapps on 1/16/15.
 */
public class DictionaryTrie {
    // ---------------- Static ----------------
    private static final Logger LOG = LoggerFactory.getLogger(DictionaryTrie.class);

    // ---------------- Constructor -----------
    DictionaryTrie() { }

    // ---------------- Private fields ---------------
    private final TrieNode root = new TrieNode("");
    private final int[] numWordsByLength = new int[BoardSize.VENTI.getN() + 1];

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
            ++numWordsByLength[word.length()];
            insertWord(word, root);
        }
        long END = System.currentTimeMillis();


        LOG.info(format("SUCCESS - loaded dictionary into DictionaryTrie in %s.", DateUtil.getDurationPretty(END - START)));
    }

    public boolean contains(String str) {
        TrieNode node = root;
        for (int pos = 0; pos < str.length(); ++pos) {
            final char c = str.charAt(pos);
            node = node.getChild(c);
            if (node == null) {
                return false;
            }
        }
        return node.isValidWord();
    }

    public boolean isPrefix(String str) {
        TrieNode node = root;
        for (int pos = 0; pos < str.length(); ++pos) {
            final char c = str.charAt(pos);
            node = node.getChild(c);
            if (node == null) {
                return false;
            }
        }
        return true;
    }

    // --------------- Private ---------------

    private static void insertWord(String word, TrieNode trieNode) {
        if (word.isEmpty()) {
            trieNode.setValidWord(true);
            return;
        }
        char c = word.charAt(0);
        TrieNode child = trieNode.addChild(c);
        insertWord(word.substring(1), child);
    }

}
