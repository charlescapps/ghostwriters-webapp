package net.capps.word.util;

import com.google.common.collect.ImmutableList;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by charlescapps on 1/27/15.
 */
public class RandomOrderIterator<T> implements Iterator<T> {
    private final ImmutableList<Integer> permutation;
    private final List<T> values;
    private final int N;

    private int index = 0;

    public RandomOrderIterator(ImmutableList<Integer> permutation, List<T> values) {
        this.permutation = permutation;
        this.values = values;
        this.N = values.size();
    }

    @Override
    public boolean hasNext() {
        return index < N;
    }

    @Override
    public T next() {
        if (index >= N) {
            throw new NoSuchElementException();
        }
        int randomIndex = permutation.get(index++);
        return values.get(randomIndex);
    }

    @Override
    public void remove() {

    }
}
