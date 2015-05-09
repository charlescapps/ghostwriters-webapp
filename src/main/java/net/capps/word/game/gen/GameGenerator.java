package net.capps.word.game.gen;

import com.google.common.base.Optional;
import net.capps.word.game.board.TileSet;
import net.capps.word.game.common.Placement;

/**
 * Created by charlescapps on 1/15/15.
 */
public interface GameGenerator {
    TileSet generateRandomFinishedGame(int N, int numWords, int maxWordSize);
    Placement generateFirstPlacement(TileSet tileSet, int maxWordSize);
    Optional<Placement> findFirstValidPlacementInRandomSearch(TileSet tileSet, int maxWordSize);
}
