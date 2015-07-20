package net.capps.word.rest.models;

import net.capps.word.game.common.Pos;

/**
 * Created by charlescapps on 2/22/15.
 */
public class PosModel {
    private int r;
    private int c;

    public PosModel() {

    }

    public PosModel(int r, int c) {
        this.r = r;
        this.c = c;
    }

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }

    public Pos toPos() {
        return Pos.of(r, c);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PosModel)) {
            return false;
        }
        PosModel otherPosModel = (PosModel)o;
        return otherPosModel.r == r &&
               otherPosModel.c == c;
    }

    @Override
    public int hashCode() {
        return r ^ (c >> 8);
    }
}
