package net.capps.word.rest.models;

import net.capps.word.game.common.Pos;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by charlescapps on 2/22/15.
 */
@XmlRootElement
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
}
