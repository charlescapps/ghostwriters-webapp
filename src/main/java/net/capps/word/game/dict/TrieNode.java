package net.capps.word.game.dict;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by charlescapps on 1/17/15.
 */
public class TrieNode {
    private final Map<Character, TrieNode> branches = Maps.newHashMap();

    // Structure so we can get the words having character at position i in O(1) time.
    private final Map<Integer, Map<Character, List<TrieNode>>> levels = Maps.newHashMap();

    private boolean validWord = false;

    public TrieNode() {
    }

    public TrieNode addChild(Character c) {
        TrieNode child = branches.get(c);
        if (child == null) {
            child = new TrieNode();
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
    }

    public Collection<TrieNode> getChildren() {
        return branches.values();
    }

    // --------- Private -------
    private static void buildLevels(Map<Integer, Map<Character, List<TrieNode>>> levels, TrieNode node, int level, char branch) {
        // Add the current node to the level map.
        if (!levels.containsKey(level)) {
            levels.put(level, Maps.<Character, List<TrieNode>>newHashMap());
        }
        Map<Character, List<TrieNode>> levelMap = levels.get(level);
        if (!levelMap.containsKey(branch)) {
            levelMap.put(branch, Lists.<TrieNode>newArrayList());
        }
        levelMap.get(branch).add(node);

        // Recurse on children
        for (Character c: node.branches.keySet()) {
            TrieNode child = node.branches.get(c);
            buildLevels(levels, child, level + 1, c);
        }
    }
}
