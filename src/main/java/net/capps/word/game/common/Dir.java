package net.capps.word.game.common;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by charlescapps on 1/16/15.
 */
public enum Dir {
    S, E, N, W;


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
}
