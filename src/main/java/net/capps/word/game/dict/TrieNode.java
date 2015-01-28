package net.capps.word.game.dict;

import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Map;

/**
 * Created by charlescapps on 1/17/15.
 */
public class TrieNode {
    private final Map<Character, TrieNode> branches = Maps.newHashMap();
    private final String word;

    // Structure so we can get the words having character at position i in O(1) time.
    private final Map<Integer, TrieLevel> levels = Maps.newHashMap();

    private boolean validWord = false;

    public TrieNode(String word) {
        this.word = word;
    }

    public TrieNode addChild(Character c) {
        TrieNode child = branches.get(c);
        if (child == null) {
            child = new TrieNode(word + c);
            branches.put(c, child);
        }

        return child;
    }

    public TrieNode getChild(Character c) {
        return branches.get(c);
    }

    public boolean isLeaf() {
        return branches.isEmpty();
    }

    public void setValidWord(boolean validWord) {
        this.validWord = validWord;
    }

    public boolean isValidWord() {
        return validWord;
    }

    public void buildLevels() {
        for (Character c: branches.keySet()) {
            buildLevels(levels, branches.get(c), 0, c);
        }
        for (TrieLevel level: levels.values()) {
            level.storeValidWordNodes();
        }
    }

    public Map<Integer, TrieLevel> getLevels() {
        return levels;
    }

    public Collection<TrieNode> getChildren() {
        return branches.values();
    }

    public String getWord() {
        return word;
    }

    // --------- Private -------

    private static void buildLevels(Map<Integer, TrieLevel> levels, TrieNode node, int depth, char branch) {
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
