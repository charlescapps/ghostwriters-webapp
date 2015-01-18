package net.capps.word.game.gen;

import net.capps.word.game.board.TileSet;
import net.capps.word.game.common.Dir;
import net.capps.word.game.common.Pos;
import net.capps.word.game.dict.DictionaryWordPicker;
import net.capps.word.game.tile.Tile;
import net.capps.word.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

import static net.capps.word.game.common.Dir.S;


/**
 * Created by charlescapps on 1/13/15.
 */
public class DefaultGameGenerator implements GameGenerator {
    private static final Random random = new Random();
    private static final Logger LOG = LoggerFactory.getLogger(DefaultGameGenerator.class);

    @Override
    public TileSet generateRandomFinishedGame(int N, int numWords, int maxWordSize) {
        TileSet tileSet = new TileSet(N);
        // TODO: Implement this!
        generateFirstMove(tileSet, maxWordSize);
        return tileSet;
    }

    private void generateFirstMove(TileSet tileSet, int maxWordSize) {
        Tile[][] tiles = tileSet.tiles;
        final int N = tileSet.N;
        final String word = DictionaryWordPicker.getInstance().getRandomWordEqualProbabilityByLength(maxWordSize);
        final int len = word.length();
        final Dir dir = Dir.randomDir();

        int minStartPos = Math.max(0, N / 2 - len + 1);
        int maxStartPos = N / 2;

        int startPos = RandomUtil.randomInt(minStartPos, maxStartPos);
        Pos pos = dir == S ? new Pos(startPos, N / 2) : new Pos(N / 2, startPos);

        tileSet.placeWord(word, pos, dir);

    }
}
