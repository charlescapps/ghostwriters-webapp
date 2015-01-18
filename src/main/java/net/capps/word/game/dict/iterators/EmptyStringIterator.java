package net.capps.word.game.dict.iterators;

import java.util.Iterator;

/**
 * Created by charlescapps on 1/17/15.
 */
public class EmptyStringIterator implements Iterator<String> {
    public static final EmptyStringIterator INSTANCE = new EmptyStringIterator();

    private EmptyStringIterator() { }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public String next() {
        return null;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
