package net.capps.word.game.common;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by charlescapps on 1/16/15.
 */
public enum Dir {
    S, E, N, W;

    public static final Dir[] VALID_PLAY_DIRS = new Dir[] { S, E };

    public static Dir randomPlayDir() {
        boolean b = ThreadLocalRandom.current().nextBoolean();
        return b ? S : E;
    }

    public boolean isValidPlayDir() {
        return this == E || this == S;
    }

    public Dir negate() {
        switch (this) {
            case S: return N;
            case E: return W;
            case N: return S;
            case W: return E;
        }
        throw new IllegalStateException();
    }

    public Dir perp() {
        switch (this) {
            case S: return E;
            case E: return S;
            case N: return W;
            case W: return N;
        }
        throw new IllegalStateException();
    }
}
