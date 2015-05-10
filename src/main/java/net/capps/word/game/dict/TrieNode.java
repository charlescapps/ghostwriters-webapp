package net.capps.word.game.dict;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by charlescapps on 1/17/15.
 */
public class TrieNode {
    public static final TrieNode[] EMPTY_TRIE_NODE_ARRAY = { };

    private Map<Character, TrieNode> branches;
    private final String word;

    private boolean validWord = false;

    public TrieNode(String word) {
        this.word = word;
    }

    public TrieNode addChild(char c) {
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

    public String getWord() {
        return word;
    }

}
