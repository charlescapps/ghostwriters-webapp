package net.capps.word.game.dict.iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by charlescapps on 1/17/15.
 */
public class DelegatingIterator<T> implements Iterator<T> {
    private final Iterator<T>[] delegates;
    private final int N;
    private int index = 0;

    public DelegatingIterator(Iterator<T>[] delegates) {
        this.delegates = delegates;
        N = delegates.length;
    }

    @Override
    public boolean hasNext() {
        while(index < N && !delegates[index].hasNext()) {
            ++index;
        }
        return index < N;
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return delegates[index].next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
