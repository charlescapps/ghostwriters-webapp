package net.capps.word.game.common;

import java.util.Random;

/**
 * Created by charlescapps on 1/16/15.
 */
public enum Dir {
    S, E;

    private static final Random random = new Random();

    public static Dir randomDir() {
        boolean b = random.nextBoolean();
        return b ? S : E;
    }
}
