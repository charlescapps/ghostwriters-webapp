package net.capps.word.game.common;

import java.util.Random;

/**
 * Created by charlescapps on 1/16/15.
 */
public enum Dir {
    S, E, N, W;

    private static final Random random = new Random();

    public static Dir randomPlayDir() {
        boolean b = random.nextBoolean();
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
}
