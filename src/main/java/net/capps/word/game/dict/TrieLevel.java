package net.capps.word.game.dict;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by charlescapps on 1/17/15.
 */
public class TrieLevel {
    // Structure so we can get the words having character at position i in O(1) time.
    private static final Map<Character, List<TrieNode>> EMPTY_NODES_BY_CHAR = ImmutableMap.of();
    private Map<Character, List<TrieNode>> nodesByChar = EMPTY_NODES_BY_CHAR;
    private TrieNode[] validWordNodes;

    public TrieLevel() {
    }

    public Map<Character, List<TrieNode>> getNodesByChar() {
        return nodesByChar;
    }

    public boolean containsKey(char c) {
        return nodesByChar.containsKey(c);
    }

    public TrieNode[] getValidWordNodes() {
        return validWordNodes;
    }

    public void addNode(char c, TrieNode node) {
        if (!nodesByChar.containsKey(c)) {
            if (nodesByChar == EMPTY_NODES_BY_CHAR) {
                nodesByChar = new HashMap<>();
            }
            nodesByChar.put(c, Lists.newArrayList(node));
        } else {
            nodesByChar.get(c).add(node);
        }
    }

    public void storeValidWordNodes() {
        List<TrieNode> validWordNodeList = new ArrayList<>();
        for (List<TrieNode> nodes: nodesByChar.values()) {
            for (TrieNode node: nodes) {
                if (node.isValidWord()) {
                    validWordNodeList.add(node);
                }
            }
        }
        validWordNodes = validWordNodeList.isEmpty() ?
                TrieNode.EMPTY_TRIE_NODE_ARRAY :
                validWordNodeList.toArray(new TrieNode[validWordNodeList.size()]);
    }
}
