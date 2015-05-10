package net.capps.word.game.dict.iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by charlescapps on 5/9/15.
 */
public class ArrayIterator<T> implements Iterator<T> {
    private final T[] array;
    private int index = 0;

    public ArrayIterator(T[] array) {
        this.array = array;
    }

    @Override
    public boolean hasNext() {
        return index < array.length;
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return array[index++];
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
