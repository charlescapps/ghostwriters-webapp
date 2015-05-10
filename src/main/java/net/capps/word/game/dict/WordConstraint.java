package net.capps.word.game.dict;

import com.google.common.base.Predicate;

/**
 * Created by charlescapps on 1/17/15.
 */
public class WordConstraint implements Predicate<String> {
    public final int pos;
    public final char c;

    public WordConstraint(int pos, char c)  {
        if (!Character.isAlphabetic(c)) {
            throw new IllegalArgumentException(String.format("Invalid alphabetic character for WordConstraint: '%c'", c));
        }
        this.pos = pos;
        this.c = Character.toUpperCase(c);
    }

    public static WordConstraint of(int pos, char c) {
        return new WordConstraint(pos, c);
    }

    @Override
    public boolean apply(String s) {
        return s != null &&
               s.charAt(pos) == c;
    }

    @Override
    public String toString() {
        return String.format("Constraint { pos=%d, c=%c }", pos, c);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof WordConstraint)) {
            return false;
        }
        WordConstraint wc = (WordConstraint)o;
        return wc.pos == pos && wc.c == c;
    }

    @Override
    public int hashCode() {
        return pos + (c >> 8);
    }
}
