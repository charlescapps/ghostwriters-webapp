package net.capps.word.game.dict.iterators;

import com.google.common.collect.Lists;
import net.capps.word.game.dict.TrieLevel;
import net.capps.word.game.dict.TrieNode;
import net.capps.word.game.dict.WordConstraint;
import net.capps.word.util.RandomUtil;

import java.util.Iterator;
import java.util.List;

/**
 * Created by charlescapps on 1/17/15.
 */
public class RandomWordIteratorFactory {

    public static Iterator<String> create(TrieNode node, List<WordConstraint> constraints, byte len) {
        if (len < 0) {
            throw new IllegalArgumentException("Cannot have 0 length! Found len=" + len);
        }

        if (len == 0) {
            if (node.isValidWord()) {
                return new SingletonIterator<>(node.getWord());
            } else {
                return EmptyStringIterator.INSTANCE;
            }
        }

        if (constraints.isEmpty()) {
            return WordsOfLengthIterator.create(node, len);
        }

        WordConstraint constraint = constraints.get(0);
        List<WordConstraint> remaining = shiftConstraints(constraints.subList(1, constraints.size()), (byte)(constraint.pos + 1)) ;

        TrieLevel level = node.getLevels().get(constraint.pos);

        if (level == null || !level.containsKey(constraint.c)) {
            return EmptyStringIterator.INSTANCE;
        }

        List<TrieNode> descendants = level.getNodesByChar().get(constraint.c);

        final int N = descendants.size();
        Iterator<String>[] delegates = new Iterator[N];

        for (int i = 0; i < N; i++) {
            TrieNode descendant = descendants.get(i);
            Iterator<String> delegate = create(descendant, remaining, (byte)(len - constraint.pos - 1));
            delegates[i] = delegate;
        }

        // Shuffle the delegates in place, so that we traverse in a random order.
        RandomUtil.shuffleInPlace(delegates);
        return new DelegatingIterator<>(delegates);
    }

    private static List<WordConstraint> shiftConstraints(List<WordConstraint> constraints, byte len) {
        List<WordConstraint> shifted = Lists.newArrayListWithCapacity(constraints.size());
        for (WordConstraint constraint: constraints) {
            shifted.add(new WordConstraint((byte)(constraint.pos - len), constraint.c));
        }
        return shifted;
    }

}
