package net.capps.word.game.common;

import net.capps.word.rest.providers.UserRecordChange;

/**
 * Created by charlescapps on 1/24/15.
 */
public enum GameResult {
    OFFERED, IN_PROGRESS, REJECTED, PLAYER1_WIN, PLAYER2_WIN, TIE, PLAYER1_TIMEOUT, PLAYER2_TIMEOUT, PLAYER1_RESIGN, PLAYER2_RESIGN;

    public UserRecordChange getPlayer1RecordChange() {
        switch (this) {
            case PLAYER1_WIN: return UserRecordChange.INCREASE_WINS;
            case PLAYER2_WIN: return UserRecordChange.INCREASE_LOSSES;
            case TIE: return UserRecordChange.INCREASE_TIES;
            case PLAYER1_RESIGN: return UserRecordChange.INCREASE_LOSSES;
            case PLAYER2_RESIGN: return UserRecordChange.INCREASE_WINS;
            default: return UserRecordChange.NOTHING;
        }
    }

    public UserRecordChange getPlayer2RecordChange() {
        switch (this) {
            case PLAYER1_WIN: return UserRecordChange.INCREASE_LOSSES;
            case PLAYER2_WIN: return UserRecordChange.INCREASE_WINS;
            case TIE: return UserRecordChange.INCREASE_TIES;
            case PLAYER2_RESIGN: return UserRecordChange.INCREASE_LOSSES;
            case PLAYER1_RESIGN: return UserRecordChange.INCREASE_WINS;
            default: return UserRecordChange.NOTHING;
        }
    }

    public double getKScale() {
        switch (this) {
            case PLAYER1_RESIGN:
            case PLAYER2_RESIGN:
                return 0.3d;
            default: return 1.0d;
        }
    }
}
