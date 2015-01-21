package net.capps.word.game.common;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by charlescapps on 1/20/15.
 */
public class PosIterator implements Iterator<Pos> {
    private final int N;

    private int r = 0, c = 0;

    public PosIterator(int N) {
        this.N = N;
    }

    @Override
    public boolean hasNext() {
        return r < N;
    }

    @Override
    public Pos next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        Pos p = Pos.of(r, c, N);

        c = (c + 1) % N;

        if (c == 0) {
            ++r;
        }
        return p;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
