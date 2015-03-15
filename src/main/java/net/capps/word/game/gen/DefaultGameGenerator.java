package net.capps.word.game.gen;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.capps.word.game.board.TileSet;
import net.capps.word.game.common.Dir;
import net.capps.word.game.common.Placement;
import net.capps.word.game.common.Pos;
import net.capps.word.game.dict.Dictionaries;
import net.capps.word.game.dict.DictionaryTrie;
import net.capps.word.game.dict.WordConstraint;
import net.capps.word.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

import static net.capps.word.game.common.Dir.S;


/**
 * Created by charlescapps on 1/13/15.
 */
public class DefaultGameGenerator implements GameGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultGameGenerator.class);
    private static final PositionLists POSITION_LISTS = PositionLists.getInstance();

    private static final DictionaryTrie TRIE = Dictionaries.getAllWordsTrie();

    public DefaultGameGenerator() {
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

        for (int i = 1; i < numWords; i++) {
            Optional<Placement> validPlacementOpt = findFirstValidPlacementInRandomSearch(tileSet, maxWordSize);
            if (!validPlacementOpt.isPresent()) {
                LOG.error("ERROR - couldn't find placement for board:\n{}", tileSet);
                throw new IllegalStateException("Could not find any valid placements!!");
            }
            Placement placement = validPlacementOpt.get();
            // LOG.trace("Placing word: " + placement);
            tileSet.placeWord(placement);
        }

        return tileSet;
    }

    @Override
    public void generateRandomWord(TileSet tileSet, int maxWordSize) {
        if (tileSet.isEmpty()) {
            Placement placement = generateFirstPlacement(tileSet, maxWordSize);
            tileSet.placeWord(placement);
            return;
        }
        Optional<Placement> placementOpt = findFirstValidPlacementInRandomSearch(tileSet, maxWordSize);
        if (!placementOpt.isPresent()) {
            throw new IllegalStateException("Failed to find a valid placement!");
        }
        tileSet.placeWord(placementOpt.get());
    }

    private Placement generateFirstPlacement(TileSet tileSet, int maxWordSize) {
        final int N = tileSet.N;
        final String word = Dictionaries.getAllWordsPicker().getRandomWordEqualProbabilityByLength(maxWordSize);
        final int len = word.length();
        final Dir dir = Dir.randomPlayDir();

        int minStartPos = Math.max(0, N / 2 - len + 1);
        int maxStartPos = Math.min(N / 2, N - len);

        int startPos = RandomUtil.randomInt(minStartPos, maxStartPos);
        Pos pos = dir == S ? Pos.of(startPos, N / 2) : Pos.of(N / 2, startPos);

        return new Placement(word, pos, dir);
    }

    private Optional<Placement> findFirstValidPlacementInRandomSearch(TileSet tileSet, int maxWordSize) {
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
        List<Integer> diffsToTry = Lists.newArrayList();

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

            List<WordConstraint> wcs = Lists.newArrayList();

            // Get all constraints from existing tiles.
            for (int j = 0; j <= totalDiff; j++) {
                Pos p1 = start.go(dir, j);
                if (tileSet.isOccupied(p1)) {
                    wcs.add(WordConstraint.of(j, tileSet.getLetterAt(p1)));
                }
            }

            Iterator<String> iter = TRIE.getWordsWithConstraintsInRandomOrder(wcs, totalDiff + 1);

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
