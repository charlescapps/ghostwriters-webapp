package net.capps.word.game.dict;

import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by charlescapps on 1/17/15.
 */
public class RandomWordIterator implements Iterator<String> {
    private final TrieNode node;
    private final List<Iterator<String>> delegates = Lists.newArrayList();
    private final int len;
    private boolean returnedSingleton = false;

    public RandomWordIterator(TrieNode node, List<WordConstraint> constraints, int len) {
        this.node = node;
        this.len = len;

        if (len == 0) {
            return;
        }

        if (constraints.isEmpty()) {
            delegates.add(new WordsOfLengthIterator(node, len));
            return;
        }

        WordConstraint constraint = constraints.get(0);
        List<WordConstraint> remaining = shiftConstraints(constraints.subList(1, constraints.size()), constraint.pos + 1) ;
        TrieLevel level = node.getLevels().get(constraint.pos);

        if (level == null || !level.containsKey(constraint.c)) {
            return;
        }

        List<TrieNode> descendents = level.getNodesByChar().get(constraint.c);

        for (TrieNode descendent: descendents) {
            Iterator<String> delegate = new RandomWordIterator(descendent, remaining, len - constraint.pos - 1);
            delegates.add(delegate);
        }
    }

    @Override
    public boolean hasNext() {
        if (len == 0) {
            return node.isValidWord() && !returnedSingleton;
        }

        for (Iterator<String> delegate: delegates) {
            if (delegate.hasNext()) {
              return true;
            }
        }
        return false;
    }

    @Override
    public String next() {
        if (len == 0) {
            if (node.isValidWord() && !returnedSingleton) {
                returnedSingleton = true;
                return node.getWord();
            }
            throw new NoSuchElementException();
        }

        for (Iterator<String> delegate: delegates) {
            if (delegate.hasNext()) {
                return node.getWord() + delegate.next();
            }
        }
        throw new NoSuchElementException();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("RandomWordIterator doesn't support removing nodes!");
    }

    private static List<WordConstraint> shiftConstraints(List<WordConstraint> constraints, int len) {
        List<WordConstraint> shifted = Lists.newArrayList();
        for (WordConstraint constraint: constraints) {
            shifted.add(new WordConstraint(constraint.pos - len, constraint.c));
        }
        return shifted;
    }

}
