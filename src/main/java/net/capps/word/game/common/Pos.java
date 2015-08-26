package net.capps.word.game.common;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import net.capps.word.rest.models.PosModel;

import java.util.List;

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

    public Pos s() {
        return new Pos(r + 1, c);
    }

    public Pos s(int n) {
        return new Pos(r + n, c);
    }

    public Pos e() {
        return new Pos(r, c + 1);
    }

    public Pos e(int n) {
        return new Pos(r, c + n);
    }

    public Pos n() {
        return new Pos(r - 1, c);
    }

    public Pos n(int n) {
        return new Pos(r - n, c);
    }

    public Pos w() {
        return new Pos(r, c - 1);
    }

    public Pos w(int n) {
        return new Pos(r, c - n);
    }

    public Pos go(Dir dir) {
        switch (dir) {
            case E: return new Pos(r, c + 1);
            case S: return new Pos(r + 1, c);
            case W: return new Pos(r, c - 1);
            case N: return new Pos(r - 1, c);
        }
        throw new IllegalStateException();
    }

    public Pos go(final Dir dir, final int num) {
        switch (dir) {
            case E: return new Pos(r, c + num);
            case S: return new Pos(r + num, c);
            case W: return new Pos(r, c - num);
            case N: return new Pos(r - num, c);
        }
        throw new IllegalStateException();
    }

    public List<Pos> adjacents() {
        return Lists.newArrayList(n(), s(), e(), w());
    }

    public int minus(Pos other) {
        if (r == other.r) {
            return c - other.c;
        } else if (c == other.c) {
            return r - other.r;
        }
        throw new IllegalStateException("Cannot subtract 2 positions that aren't on the same row or column!");
    }

    public Dir getDirTo(Pos pos) {
        if (r == pos.r) {
            if (c <= pos.c) {
                return Dir.E;
            }
            return Dir.W;
        }
        if (c == pos.c) {
            if (r <= pos.r) {
                return Dir.S;
            }
            return Dir.N;
        }
        throw new IllegalStateException("Cannot subtract 2 positions that aren't on the same row or column!");
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("r", r)
                .add("c", c)
                .toString();
    }

    public PosModel toPosModel() {
        return new PosModel(r, c);
    }

    public MutPos toMutPos() {
        return new MutPos(r, c);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pos)) {
            return false;
        }
        Pos other = (Pos) o;
        return r == other.r && c == other.c;
    }

    @Override
    public int hashCode() {
        return r ^ (c >> 8);
    }
}
