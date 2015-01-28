package net.capps.word.game.dict;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * Created by charlescapps on 1/17/15.
 */
public class TrieLevel {
    // Structure so we can get the words having character at position i in O(1) time.
    private final Map<Character, List<TrieNode>> nodesByChar = Maps.newHashMap();
    private final List<Character> chars = Lists.newArrayList();
    private TrieNode[] validWordNodes;

    public TrieLevel() {
    }

    public Map<Character, List<TrieNode>> getNodesByChar() {
        return nodesByChar;
    }

    public List<Character> getChars() {
        return chars;
    }

    public boolean containsKey(char c) {
        return nodesByChar.containsKey(c);
    }

    public TrieNode[] getValidWordNodes() {
        return validWordNodes;
    }

    public void addNode(char c, TrieNode node) {
        if (!nodesByChar.containsKey(c)) {
            nodesByChar.put(c, Lists.newArrayList(node));
        } else {
            nodesByChar.get(c).add(node);
        }

        if (!chars.contains(c)) {
            chars.add(c);
        }
    }

    public void finalize() {
        List<TrieNode> validWordNodeList = Lists.newArrayList();
        for (List<TrieNode> nodes: nodesByChar.values()) {
            for (TrieNode node: nodes) {
                if (node.isValidWord()) {
                    validWordNodeList.add(node);
                }
            }
        }
        validWordNodes = validWordNodeList.toArray(new TrieNode[validWordNodeList.size()]);
    }
}
