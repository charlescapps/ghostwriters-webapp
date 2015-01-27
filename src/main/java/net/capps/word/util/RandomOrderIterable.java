package net.capps.word.util;

import com.google.common.collect.ImmutableList;

import java.util.Iterator;
import java.util.List;

/**
 * Created by charlescapps on 1/27/15.
 */
public class RandomOrderIterable<T> implements Iterable<T> {
    private final ImmutableList<Integer> permutation;
    private final List<T> values;

    public RandomOrderIterable(ImmutableList<Integer> permutation, List<T> values) {
        this.permutation = permutation;
        this.values = values;
    }

    @Override
    public Iterator<T> iterator() {
        return new RandomOrderIterator<>(permutation, values);
    }
}
