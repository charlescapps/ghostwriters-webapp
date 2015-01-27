package net.capps.word.game.gen;

import net.capps.word.game.board.TileSet;

/**
 * Created by charlescapps on 1/15/15.
 */
public interface GameGenerator {
    TileSet generateRandomFinishedGame(int N, int numWords, int maxWordSize);
    void generateRandomWord(TileSet tileSet, int maxWordSize);
}
