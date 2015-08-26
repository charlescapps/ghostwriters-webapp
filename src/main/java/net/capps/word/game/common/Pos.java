package net.capps.word.game.common;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import net.capps.word.rest.models.PosModel;

import java.util.List;

/**
 * Created by charlescapps on 1/16/15.
 */
public class Pos {
    private static final int MIN_ROW = -1;
    private static final int MAX_ROW = 13;
    private static final int SIZE = MAX_ROW - MIN_ROW + 1;
    private static final Pos[][] FLYWEIGHT = new Pos[SIZE][SIZE];

    static {
        for (int i = 0; i < SIZE; ++i) {
            for (int j = 0; j < SIZE; ++j) {
                FLYWEIGHT[i][j] = new Pos(MIN_ROW + i, MIN_ROW + j);
            }
        }
    }

    public final int r;
    public final int c;

    private Pos(int r, int c) {
        this.r = r;
        this.c = c;
    }

    /**
     * Get an instance of a Pos for the given r, c.
     * PRECONDITION: Must have r and c are between MIN_ROW and MAX_ROW
     * @param r
     * @param c
     * @return
     */
    public static Pos of(int r, int c) {
        return FLYWEIGHT[r - MIN_ROW][c - MIN_ROW];
    }

    public static Pos ofSafe(int r, int c) {
        if (r >= MIN_ROW && r <= MAX_ROW && c >= MIN_ROW && c <= MAX_ROW) {
            return FLYWEIGHT[r - MIN_ROW][c - MIN_ROW];
        }
        return new Pos(r, c);
    }

    public Pos s() {
        return of(r + 1, c);
    }

    public Pos s(int n) {
        return of(r + n, c);
    }

    public Pos e() {
        return of(r, c + 1);
    }

    public Pos e(int n) {
        return of(r, c + n);
    }

    public Pos n() {
        return of(r - 1, c);
    }

    public Pos n(int n) {
        return of(r - n, c);
    }

    public Pos w() {
        return of(r, c - 1);
    }

    public Pos w(int n) {
        return of(r, c - n);
    }

    public Pos go(Dir dir) {
        switch (dir) {
            case E: return of(r, c + 1);
            case S: return of(r + 1, c);
            case W: return of(r, c - 1);
            case N: return of(r - 1, c);
        }
        throw new IllegalStateException();
    }

    public Pos go(final Dir dir, final int num) {
        switch (dir) {
            case E: return of(r, c + num);
            case S: return of(r + num, c);
            case W: return of(r, c - num);
            case N: return of(r - num, c);
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
