package net.capps.word.game.gen;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.capps.word.game.board.TileSet;
import net.capps.word.game.common.Dir;
import net.capps.word.game.common.Placement;
import net.capps.word.game.common.Pos;
import net.capps.word.game.dict.Dictionaries;
import net.capps.word.game.dict.DictionaryWordSets;
import net.capps.word.game.dict.WordConstraint;
import net.capps.word.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static net.capps.word.game.common.Dir.S;


/**
 * Created by charlescapps on 1/13/15.
 */
public class DefaultGameGenerator implements GameGenerator {
    private static final DefaultGameGenerator INSTANCE = new DefaultGameGenerator(Dictionaries.getEnglishWordSets());

    private static final Logger LOG = LoggerFactory.getLogger(DefaultGameGenerator.class);
    private static final PositionLists POSITION_LISTS = PositionLists.getInstance();
    private static final long MAX_TIME_MILLIS = TimeUnit.SECONDS.toMillis(24);
    private static final int MIN_WORDS = 5;
    private final DictionaryWordSets dictWordSets;

    public static DefaultGameGenerator getInstance() {
        return INSTANCE;
    }

    public DefaultGameGenerator(DictionaryWordSets dictWordSets) {
        this.dictWordSets = dictWordSets;
    }

    @Override
    public TileSet generateRandomFinishedGame(int N, int numWords, int maxWordSize) {
        Preconditions.checkArgument(numWords > 0, "numWords must be > 0");
        Preconditions.checkArgument(maxWordSize >= 2, "maxWordSize must be at least 2");
        Preconditions.checkArgument(N >= 5, "N must be at least 5");

        TileSet tileSet = new TileSet(N);

        // Place the first placement
        Placement firstPlacement = generateFirstPlacement(tileSet, maxWordSize);
        tileSet.placeWord(firstPlacement);

        final long START = System.currentTimeMillis();
        for (int i = 1; i < numWords; i++) {
            // If it's taking more than 24s to create a game, then return what we have.
            if (System.currentTimeMillis() - START > MAX_TIME_MILLIS && i >= MIN_WORDS) {
                break;
            }
            Optional<Placement> validPlacementOpt = findFirstValidPlacementInRandomSearch(tileSet, maxWordSize);
            if (!validPlacementOpt.isPresent()) {
                LOG.error("ERROR - couldn't find placement for board:\n{}", tileSet);
                return tileSet;
            }
            Placement placement = validPlacementOpt.get();
            // LOG.trace("Placing word: " + placement);
            tileSet.placeWord(placement);
        }

        return tileSet;
    }

    @Override
    public Placement generateFirstPlacement(TileSet tileSet, int maxWordSize) {
        final int N = tileSet.N;
        final String word = dictWordSets.getRandomWordBetweenLengths(2, maxWordSize);
        final int len = word.length();
        final Dir dir = Dir.randomPlayDir();

        int minStartPos = Math.max(0, N / 2 - len + 1);
        int maxStartPos = Math.min(N / 2, N - len);

        int startPos = RandomUtil.randomInt(minStartPos, maxStartPos);
        Pos pos = dir == S ? Pos.of(startPos, N / 2) : Pos.of(N / 2, startPos);

        return new Placement(word, pos, dir);
    }

    @Override
    public Optional<Placement> findFirstValidPlacementInRandomSearch(TileSet tileSet, int maxWordSize) {
        final int N = tileSet.N;

        ImmutableList<Pos> positions = POSITION_LISTS.getPositionList(N);

        // Search the possible start positions in a random order.
        List<Pos> randomOrderPositions = RandomUtil.shuffleList(positions);

        for (Pos p: randomOrderPositions) {
            if (!tileSet.isOccupied(p)) {
                Dir[] randomOrderDirs = RandomUtil.shuffleArray(Dir.VALID_PLAY_DIRS);
                for (Dir dir: randomOrderDirs) {
                    Optional<Placement> optValidPlacement = getFirstValidPlacementFromUnoccupiedStartTile(tileSet, p, dir, maxWordSize);
                    if (optValidPlacement.isPresent()) {
                        return optValidPlacement;
                    }
                }
            }
        }

        return Optional.empty();
    }

    private Optional<Placement> getFirstValidPlacementFromUnoccupiedStartTile(TileSet tileSet, Pos start, Dir dir, int maxWordSize) {
        // Precondition: the start pos isn't an occupied tile.

        Pos occOrAdj = tileSet.getFirstOccupiedOrAdjacent(start, dir, maxWordSize);

        if (null == occOrAdj) {
            occOrAdj = start;
        }

        // If the tile in the reverse direction is occupied, we must consider our play including all occupied tiles
        // in that direction.
        Pos p = tileSet.getEndOfOccupied(start, dir.negate());
        start = p;

        final int startDiff = Math.max(1, occOrAdj.minus(start));

        // Do not append to an existing word.
        if (startDiff > 1) {
            return Optional.empty();
        }

        int maxSearched = -1;

        // Compute possible diffs from the current position to place words at, i.e. possible lengths of words
        List<Integer> diffsToTry = new ArrayList<>();

        p = p.go(dir, startDiff);
        for (int i = startDiff; i < maxWordSize; ++i, p = p.go(dir)) {
            if (i <= maxSearched) {
                continue;
            }
            if (!tileSet.isValid(p)) {
                break;
            }

            Pos wordEndPos = tileSet.getEndOfOccupied(p, dir);
            int totalDiff = wordEndPos.minus(start);

            maxSearched = Math.max(maxSearched, totalDiff);
            diffsToTry.add(totalDiff);
        }

        // Try all possible word lengths in a random order.
        RandomUtil.shuffleInPlace(diffsToTry);

        for (int totalDiff : diffsToTry) {

            List<WordConstraint> wcs = new ArrayList<>();

            // Get all constraints from existing tiles.
            Pos scan = start;
            for (int j = 0; j <= totalDiff; ++j, scan = scan.go(dir)) {
                if (tileSet.isOccupied(scan)) {
                    wcs.add(new WordConstraint(j, tileSet.getLetterAt(scan)));
                }
            }

            Iterator<String> iter = dictWordSets.getWordsWithConstraintsInRandomOrder(wcs, totalDiff + 1);

            while (iter.hasNext()) {
                String word = iter.next();
                Placement placement = new Placement(word, start, dir);
                if (tileSet.isValidPerpendicularPlacement(placement, null)) {
                    return Optional.of(placement);
                }
            }
        }

        return Optional.empty();
    }

}
