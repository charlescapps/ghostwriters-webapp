package net.capps.word.game.common;

/**
 * Created by charlescapps on 8/25/15.
 */
public class MutPos {
    public int r;
    public int c;

    public MutPos(int r, int c) {
        this.r = r;
        this.c = c;
    }

    public MutPos(Pos p) {
        this.r = p.r;
        this.c = p.c;
    }

    public MutPos(MutPos p) {
        this.r = p.r;
        this.c = p.c;
    }

    public Pos toPos() {
        return new Pos(r, c);
    }

    public void go(Dir d) {
        switch (d) {
            case E: ++c; return;
            case S: ++r; return;
            case W: --c; return;
            case N: --r; return;
        }
        throw new IllegalArgumentException("Invalid dir: " + d);
    }

    public void go(Dir d, int n) {
        switch (d) {
            case E: c += n; return;
            case S: r += n; return;
            case W: c -= n; return;
            case N: r -= n; return;
        }
        throw new IllegalArgumentException("Invalid dir: " + d);
    }

    public int minus(Pos other) {
        if (r == other.r) {
            return c - other.c;
        } else if (c == other.c) {
            return r - other.r;
        }
        throw new IllegalStateException("Cannot subtract 2 positions that aren't on the same row or column!");
    }

    public int minus(MutPos other) {
        if (r == other.r) {
            return c - other.c;
        } else if (c == other.c) {
            return r - other.r;
        }
        throw new IllegalStateException("Cannot subtract 2 positions that aren't on the same row or column!");
    }

    public boolean isEquivalent(Pos p) {
        return p.r == r && p.c == c;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MutPos)) {
            return false;
        }

        MutPos other = (MutPos)o;
        return other.r == r && other.c == c;
    }

    @Override
    public int hashCode() {
        return r + c << 8;
    }

    public Dir getDirTo(MutPos pos) {
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
}
