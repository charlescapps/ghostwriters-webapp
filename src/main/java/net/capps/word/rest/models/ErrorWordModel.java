package net.capps.word.rest.models;

import net.capps.word.game.common.Dir;

/**
 * Created by charlescapps on 10/11/15.
 *
 * Client knows what tiles are being placed on the board -
 * just needs the start position and direction of the erroneous word.
 */
public class ErrorWordModel {
    private PosModel start;
    private Dir dir;

    public ErrorWordModel(PosModel start, Dir dir) {
        this.start = start;
        this.dir = dir;
    }

    public PosModel getStart() {
        return start;
    }

    public Dir getDir() {
        return dir;
    }
}
