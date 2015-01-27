package net.capps.word.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by charlescapps on 1/27/15.
 */
public class SingleValueIterator<T> implements Iterator<T> {
    private final T value;
    private boolean returnedTheValue = false;

    public SingleValueIterator(T value) {
        this.value = value;
    }

    @Override
    public boolean hasNext() {
        return !returnedTheValue;
    }

    @Override
    public T next() {
        if (returnedTheValue) {
            throw new NoSuchElementException();
        }
        returnedTheValue = true;
        return value;
    }

    @Override
    public void remove() {

    }
}
