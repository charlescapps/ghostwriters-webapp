package net.capps.word.game.dict.iterators;

import com.google.common.collect.Lists;
import net.capps.word.game.dict.TrieLevel;
import net.capps.word.game.dict.TrieNode;
import net.capps.word.game.dict.WordConstraint;
import net.capps.word.util.PermutationUtil;
import net.capps.word.util.RandomUtil;

import java.util.Iterator;
import java.util.List;

/**
 * Created by charlescapps on 1/17/15.
 */
public class RandomWordIteratorFactory {
    private static final PermutationUtil permutationUtil = PermutationUtil.getInstance();

    public static Iterator<String> create(TrieNode node, List<WordConstraint> constraints, int len) {
        if (len < 0) {
            throw new IllegalArgumentException("Cannot have 0 length! Found len=" + len);
        }

        if (len == 0) {
            if (node.isValidWord()) {
                return Lists.newArrayList(node.getWord()).iterator();
            } else {
                return EmptyStringIterator.INSTANCE;
            }
        }

        if (constraints.isEmpty()) {
            return WordsOfLengthIterator.create(node, len);
        }

        WordConstraint constraint = constraints.get(0);
        List<WordConstraint> remaining = shiftConstraints(constraints.subList(1, constraints.size()), constraint.pos + 1) ;

        TrieLevel level = node.getLevels().get(constraint.pos);

        if (level == null || !level.containsKey(constraint.c)) {
            return EmptyStringIterator.INSTANCE;
        }

        List<TrieNode> descendents = level.getNodesByChar().get(constraint.c);

        List<Iterator<String>> delegates = Lists.newArrayList();

        for (TrieNode descendent: descendents) {
            Iterator<String> delegate = create(descendent, remaining, len - constraint.pos - 1);
            delegates.add(delegate);
        }

        List<Iterator<String>> randomOrderDelegates = RandomUtil.randomizeList(delegates);
        return new DelegatingIterator<>(randomOrderDelegates);
    }

    private static List<WordConstraint> shiftConstraints(List<WordConstraint> constraints, int len) {
        List<WordConstraint> shifted = Lists.newArrayList();
        for (WordConstraint constraint: constraints) {
            shifted.add(new WordConstraint(constraint.pos - len, constraint.c));
        }
        return shifted;
    }

}
