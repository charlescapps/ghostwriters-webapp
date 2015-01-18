package net.capps.word.game.common;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * Created by charlescapps on 1/18/15.
 */
public class Placement {
    public static final Set<Placement> EMPTY_SET = ImmutableSet.of();

    private final String word;
    private final Pos startPos;
    private final Dir dir;

    public Placement(String word, Pos startPos, Dir dir) {
        Preconditions.checkNotNull(word);
        Preconditions.checkNotNull(startPos);
        Preconditions.checkNotNull(dir);
        this.word = word;
        this.startPos = startPos;
        this.dir = dir;
    }

    public String getWord() {
        return word;
    }

    public Pos getStartPos() {
        return startPos;
    }

    public Dir getDir() {
        return dir;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("word", word)
                .add("startPos", startPos)
                .add("dir", dir)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Placement)) {
            return false;
        }
        Placement other = (Placement)o;
        return word.equals(other.word) &&
                startPos.equals(other.startPos) &&
                dir == other.dir;
    }

    @Override
    public int hashCode() {
        return word.hashCode() ^ startPos.hashCode() ^ dir.hashCode();
    }
}
