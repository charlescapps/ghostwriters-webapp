package net.capps.word.game.common;

import com.google.common.base.Preconditions;

/**
 * Created by charlescapps on 1/18/15.
 */
public enum GameDensity {
    SPARSE,REGULAR,WORD_JUNGLE;

    public int getNumWords(BoardSize boardSize) {
        Preconditions.checkNotNull(boardSize);
        switch (boardSize) {
            case TINY:
                switch(this) {
                    case SPARSE: return 4;
                    case REGULAR: return 5;
                    case WORD_JUNGLE: return 6;
                }
            case TALL:
                switch(this) {
                    case SPARSE: return 9;
                    case REGULAR: return 12;
                    case WORD_JUNGLE: return 15;
                }
            case GRANDE:
                switch(this) {
                    case SPARSE: return 12;
                    case REGULAR: return 15;
                    case WORD_JUNGLE: return 18;
                }
            case VENTI:
                switch(this) {
                    case SPARSE: return 15;
                    case REGULAR: return 18;
                    case WORD_JUNGLE: return 21;
                }
        }
        throw new IllegalStateException();
    }
}
