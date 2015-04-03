package net.capps.word.game.common;

import net.capps.word.rest.providers.UserRecordChange;

/**
 * Created by charlescapps on 1/24/15.
 */
public enum GameResult {
    IN_PROGRESS, PLAYER1_WIN, PLAYER2_WIN, TIE, PLAYER1_TIMEOUT, PLAYER2_TIMEOUT;

    public UserRecordChange getPlayer1RecordChange() {
        switch (this) {
            case PLAYER1_WIN: return UserRecordChange.INCREASE_WINS;
            case PLAYER2_WIN: return UserRecordChange.INCREASE_LOSSES;
            case TIE: return UserRecordChange.INCREASE_TIES;
            default: return UserRecordChange.NOTHING;
        }
    }

    public UserRecordChange getPlayer2RecordChange() {
        switch (this) {
            case PLAYER1_WIN: return UserRecordChange.INCREASE_LOSSES;
            case PLAYER2_WIN: return UserRecordChange.INCREASE_WINS;
            case TIE: return UserRecordChange.INCREASE_TIES;
            default: return UserRecordChange.NOTHING;
        }
    }
}
