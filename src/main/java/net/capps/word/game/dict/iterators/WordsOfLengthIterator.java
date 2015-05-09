package net.capps.word.game.dict.iterators;

import net.capps.word.game.dict.TrieLevel;
import net.capps.word.game.dict.TrieNode;
import net.capps.word.util.RandomUtil;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by charlescapps on 1/17/15.
 */
public class WordsOfLengthIterator implements Iterator<String> {
    private final TrieLevel level;
    private TrieNode[] nodes;

    private int nodeIndex = 0;

    private WordsOfLengthIterator(TrieLevel level) {
        this.level = level;
    }

    public static Iterator<String> create(TrieNode node, byte len) {
        if (len <= 0) {
            throw new IllegalArgumentException("Length cannot be <= 0");
        }

        TrieLevel level = node.getLevels().get((byte)(len - 1));
        if (level == null || level.getValidWordNodes().length <= 0) {
            return EmptyStringIterator.INSTANCE;
        }
        return new WordsOfLengthIterator(level);
    }

    @Override
    public boolean hasNext() {
        if (nodes == null) {
            initNodes();
        }
        return nodeIndex < nodes.length;
    }

    @Override
    public String next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        TrieNode node = nodes[nodeIndex];
        String word = node.getWord();
        ++nodeIndex;
        return word;
    }

    private void initNodes() {
        nodes = RandomUtil.shuffleArray(level.getValidWordNodes());
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove is unsupported.");
    }
}
