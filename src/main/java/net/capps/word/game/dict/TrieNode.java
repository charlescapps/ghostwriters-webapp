package net.capps.word.game.dict;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by charlescapps on 1/17/15.
 */
public class TrieNode {
    public static final TrieNode[] EMPTY_TRIE_NODE_ARRAY = { };
    private static final Collection<TrieNode> EMPTY_TRIE_NODE_LIST = ImmutableList.of();
    private static final Map<Integer, TrieLevel> EMPTY_LEVELS = ImmutableMap.of();

    private Map<Character, TrieNode> branches;
    private final String word;

    // Structure so we can get the words having character at position i in O(1) time.
    private Map<Integer, TrieLevel> levels;

    private boolean validWord = false;

    public TrieNode(String word) {
        this.word = word;
    }

    public TrieNode addChild(Character c) {
        if (branches == null) {
            branches = new HashMap<>();
        }
        TrieNode child = branches.get(c);
        if (child == null) {
            child = new TrieNode(word + c);
            branches.put(c, child);
        }

        return child;
    }

    public TrieNode getChild(Character c) {
        return branches == null ? null : branches.get(c);
    }

    public boolean isLeaf() {
        return branches == null || branches.isEmpty();
    }

    public void setValidWord(boolean validWord) {
        this.validWord = validWord;
    }

    public boolean isValidWord() {
        return validWord;
    }

    public void buildLevels() {
        if (isLeaf()) {
            return;
        }
        levels = new HashMap<>();
        for (Character c: branches.keySet()) {
            buildLevels(levels, branches.get(c), 0, c);
        }
        for (TrieLevel level: levels.values()) {
            level.storeValidWordNodes();
        }
    }

    public Map<Integer, TrieLevel> getLevels() {
        return levels == null ? EMPTY_LEVELS : levels;
    }

    public Collection<TrieNode> getChildren() {
        return branches == null ? EMPTY_TRIE_NODE_LIST : branches.values();
    }

    public String getWord() {
        return word;
    }

    // --------- Private -------

    private static void buildLevels(Map<Integer, TrieLevel> levels, TrieNode node, int depth, char branch) {
        if (node.isLeaf()) {
            return;
        }

        // Add the current node to the level map.
        if (!levels.containsKey(depth)) {
            levels.put(depth, new TrieLevel());
        }

        TrieLevel level = levels.get(depth);

        level.addNode(branch, node);

        // Recurse on children
        for (Character c: node.branches.keySet()) {
            TrieNode child = node.branches.get(c);
            buildLevels(levels, child, depth + 1, c);
        }
    }

}
