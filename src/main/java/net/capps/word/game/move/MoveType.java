package net.capps.word.game.move;

/**
 * Created by charlescapps on 1/22/15.
 */
public enum MoveType {
    PLAY_WORD, GRAB_TILES, PASS, RESIGN;

    public boolean isSimpleMove() {
        return this == PASS || this == RESIGN;
    }
}
