package net.capps.word.game.common;

import com.google.common.base.MoreObjects;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

/**
 * Created by charlescapps on 1/16/15.
 */
public class Pos {
    public final int r;
    public final int c;

    // (r,c,N) - how a Pos is stored when stored in the Database or in JSON
    private static final Pattern SERIAL_PATTERN = Pattern.compile("\\((\\d+),(\\d+),(\\d+)\\)");

    private Pos(int r, int c) {
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
        return go(dir, 1);
    }

    public Pos go(Dir dir, int num) {
        switch (dir) {
            case E: return e(num);
            case S: return s(num);
            case W: return w(num);
            case N: return n(num);
        }
        throw new IllegalStateException();
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
            if (r <= pos.r) {
                return Dir.E;
            }
            return Dir.W;
        }
        if (c == pos.c) {
            if (c <= pos.c) {
                return Dir.S;
            }
            return Dir.N;
        }
        throw new IllegalStateException("Cannot subtract 2 positions that aren't on the same row or column!");
    }

    public static Pos of(int r, int c, int N) {
        return new Pos(r, c);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("r", r)
                .add("c", c)
                .toString();
    }

    public String toSerializedForm() {
        StringBuilder sb = new StringBuilder();
        return sb.append("(")
                .append(r).append(",")
                .append(c)
                .append(")")
                .toString();
    }

    public static Pos fromSerializedForm(String str) {
        Matcher m = SERIAL_PATTERN.matcher(str);
        if (!m.matches()) {
            throw new IllegalArgumentException(format("Input String \"%s\" doesn't match pattern (r,c,N)", str));
        }
        String r = m.group(1);
        String c = m.group(2);
        String N = m.group(3);
        return new Pos(Integer.parseInt(r), Integer.parseInt(c));
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
        return r + (c >> 8);
    }
}
