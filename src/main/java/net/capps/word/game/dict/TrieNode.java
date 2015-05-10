package net.capps.word.game.dict;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by charlescapps on 1/17/15.
 */
public class TrieNode {
    public static final TrieNode[] EMPTY_TRIE_NODE_ARRAY = { };

    private Map<Character, TrieNode> branches;
    private final TrieNode parent;

    private boolean validWord = false;

    public TrieNode(TrieNode parent) {
        this.parent = parent;
    }

    public TrieNode addChild(Character c) {
        if (branches == null) {
            branches = new HashMap<>();
        }
        TrieNode child = branches.get(c);
        if (child == null) {
            child = new TrieNode(this);
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
        StringBuilder sb = new StringBuilder();
        TrieNode node = this;
        while (node.parent != null) {
            Map<Character, TrieNode> branches = node.parent.branches;
            for (Character c: branches.keySet()) {
                if (branches.get(c) == node) {
                    sb.append(c);
                    break;
                }
            }
            node = node.parent;
        }

        sb.reverse();
        return sb.toString();
    }

}
