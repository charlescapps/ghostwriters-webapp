package net.capps.word.game.dict.iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by charlescapps on 1/27/15.
 */
public class SingletonIterator<T> implements Iterator<T> {
    private final T item;
    private boolean hasReturnedItem = false;

    public SingletonIterator(T item) {
        this.item = item;
    }

    @Override
    public boolean hasNext() {
        return !hasReturnedItem;
    }

    @Override
    public T next() {
        if (hasReturnedItem) {
            throw new NoSuchElementException();
        }
        hasReturnedItem = true;
        return item;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
