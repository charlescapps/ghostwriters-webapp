package net.capps.word.game.dict;

import com.google.common.collect.Lists;
import net.capps.word.util.RandomUtil;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by charlescapps on 1/17/15.
 */
public class WordsOfLengthIterator implements Iterator<String> {
    private final int len;
    private final TrieNode node;
    private final TrieLevel level;
    private List<TrieNode> nodes;

    private int nodeIndex = 0;

    public WordsOfLengthIterator(TrieNode node, int len) {
        this.len = len;
        this.node = node;

        if (len <= 0) {
            throw new IllegalArgumentException("Length cannot be <= 0");
        }

        level = node.getLevels().get(len - 1);

    }

    @Override
    public boolean hasNext() {
        if (nodes == null) {
            initNodes();
        }
        return nodeIndex < nodes.size();
    }

    @Override
    public String next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        TrieNode node = nodes.get(nodeIndex);
        String word = node.getWord();
        ++nodeIndex;
        return word;
    }

    private void initNodes() {
        nodes = Lists.newArrayList();
        for (List<TrieNode> list : level.getNodesByChar().values()) {
            for (TrieNode node: list) {
                if (node.isValidWord()) {
                    nodes.add(node);
                }
            }
        }
        nodes = RandomUtil.randomizeList(nodes);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove is unsupported.");
    }
}
