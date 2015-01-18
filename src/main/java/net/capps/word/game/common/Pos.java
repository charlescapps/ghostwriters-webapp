package net.capps.word.game.common;

import com.google.common.base.MoreObjects;

/**
 * Created by charlescapps on 1/16/15.
 */
public class Pos {
    public final int r;
    public final int c;

    public Pos(int r, int c) {
        this.r = r;
        this.c = c;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("r", r)
                .add("c", c)
                .toString();
    }
}
