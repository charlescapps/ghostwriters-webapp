package net.capps.word.game.dict.iterators;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by charlescapps on 1/17/15.
 */
public class DelegatingIterator<T> implements Iterator<T> {
    private final List<Iterator<T>> delegates;
    private int index = 0;

    public DelegatingIterator(List<Iterator<T>> delegates) {
        this.delegates = delegates;
    }

    @Override
    public boolean hasNext() {
        if (index >= delegates.size()) {
            return false;
        }
        while(index < delegates.size() && !delegates.get(index).hasNext()) {
            ++index;
        }
        return index < delegates.size();
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return delegates.get(index).next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
