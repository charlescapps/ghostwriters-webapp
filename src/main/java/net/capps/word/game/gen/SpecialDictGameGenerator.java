package net.capps.word.game.gen;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.capps.word.game.board.TileSet;
import net.capps.word.game.common.Dir;
import net.capps.word.game.common.Placement;
import net.capps.word.game.common.Pos;
import net.capps.word.game.dict.DictionaryWordSets;
import net.capps.word.game.dict.WordConstraint;
import net.capps.word.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static net.capps.word.game.common.Dir.S;


/**
 * Created by charlescapps on 1/13/15.
 */
public class SpecialDictGameGenerator implements GameGenerator {
    private static final DefaultGameGenerator DEFAULT_GAME_GENERATOR = DefaultGameGenerator.getInstance();

    private static final Logger LOG = LoggerFactory.getLogger(SpecialDictGameGenerator.class);
    private static final PositionLists POSITION_LISTS = PositionLists.getInstance();
    private final DictionaryWordSets primaryWordSets;
    private final DictionaryWordSets secondaryWordSets;
    private final Set<String> usedPrimaryWords = new HashSet<>();

    public SpecialDictGameGenerator(DictionaryWordSets primaryWordSets, DictionaryWordSets secondaryWordSets) {
        this.primaryWordSets = primaryWordSets;
        this.secondaryWordSets = secondaryWordSets;
    }

    @Override
    public TileSet generateRandomFinishedGame(int N, int numWords, int maxWordSize) {
        Preconditions.checkArgument(numWords > 0, "numWords must be > 0");
        Preconditions.checkArgument(maxWordSize >= 2, "maxWordSize must be at least 2");
        Preconditions.checkArgument(N >= 5, "N must be at least 5");
        Preconditions.checkArgument(usedPrimaryWords.isEmpty(), "Cannot re-use a Special2GameGenerator instance!");

        TileSet tileSet = new TileSet(N);

        // Place the first placement
        Placement firstPlacement = generateFirstPlacement(tileSet, maxWordSize);
        tileSet.placeWord(firstPlacement);

        for (int i = 1; i < numWords; i++) {
            Optional<Placement> validPlacementOpt = findFirstValidPlacementInRandomSearch(tileSet, maxWordSize);
            if (!validPlacementOpt.isPresent()) {
                validPlacementOpt = DEFAULT_GAME_GENERATOR.findFirstValidPlacementInRandomSearch(tileSet, maxWordSize);
            }
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
        final String word = primaryWordSets.getRandomWordBetweenLengths(2, maxWordSize);
        usedPrimaryWords.add(word);
        final Dir dir = Dir.randomPlayDir();

        int startPos = 0;
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

        return Optional.absent();
    }

    private Optional<Placement> getFirstValidPlacementFromUnoccupiedStartTile(TileSet tileSet, Pos start, Dir dir, int maxWordSize) {
        // Precondition: the start pos isn't an occupied tile.

        Optional<Pos> firstOccupiedOrAdjacent = tileSet.getFirstOccupiedOrAdjacent(start, dir, maxWordSize);

        if (!firstOccupiedOrAdjacent.isPresent()) {
            return Optional.absent();
        }

        Pos occOrAdj = firstOccupiedOrAdjacent.get();

        // If the tile in the reverse direction is occupied, we must consider our play including all occupied tiles
        // in that direction.
        Pos previous = start.go(dir.negate());
        if (tileSet.isOccupied(previous)) {
            start = tileSet.getEndOfOccupied(previous, dir.negate());
        }

        final int diff = occOrAdj.minus(start);

        int maxSearched = -1;

        // Compute possible diffs from the current position to place words at, i.e. possible lengths of words
        List<Integer> diffsToTry = new ArrayList<>();

        for (int i = diff; i < maxWordSize; i++) {
            if (i <= maxSearched) {
                continue;
            }
            Pos p = start.go(dir, i);
            if (!tileSet.isValid(p)) {
                break;
            }

            Pos wordEndPos = tileSet.getEndOfOccupied(p.go(dir), dir);
            int totalDiff = wordEndPos.minus(start);

            maxSearched = Math.max(maxSearched, totalDiff);
            diffsToTry.add(totalDiff);
        }

        // Try all possible word lengths in a random order.
        RandomUtil.shuffleInPlace(diffsToTry);

        for (int totalDiff : diffsToTry) {

            List<WordConstraint> wcs = new ArrayList<>();

            // Get all constraints from existing tiles.
            for (byte j = 0; j <= totalDiff; j++) {
                Pos p1 = start.go(dir, j);
                if (tileSet.isOccupied(p1)) {
                    wcs.add(WordConstraint.of(j, tileSet.getLetterAt(p1)));
                }
            }

            // First search the Primary Dictionary, ignoring words we've already placed.
            Iterator<String> iter = primaryWordSets.getWordsWithConstraintsInRandomOrder(wcs, totalDiff + 1);

            while (iter.hasNext()) {
                String word = iter.next();
                if (usedPrimaryWords.contains(word)) {
                    continue;
                }
                Placement placement = new Placement(word, start, dir);
                if (!tileSet.getPlacementError(placement).isPresent()) {
                    return Optional.of(placement);
                }
            }

            // Next, search the Secondary Dictionary.
            iter = secondaryWordSets.getWordsWithConstraintsInRandomOrder(wcs, totalDiff + 1);

            while (iter.hasNext()) {
                String word = iter.next();
                Placement placement = new Placement(word, start, dir);
                if (!tileSet.getPlacementError(placement).isPresent()) {
                    return Optional.of(placement);
                }
            }
        }

        return Optional.absent();
    }

}
