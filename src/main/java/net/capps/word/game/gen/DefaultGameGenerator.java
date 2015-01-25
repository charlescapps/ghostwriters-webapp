package net.capps.word.game.gen;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.capps.word.game.board.TileSet;
import net.capps.word.game.common.Dir;
import net.capps.word.game.common.Placement;
import net.capps.word.game.common.Pos;
import net.capps.word.game.dict.DictionaryTrie;
import net.capps.word.game.dict.DictionaryWordPicker;
import net.capps.word.game.dict.WordConstraint;
import net.capps.word.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static net.capps.word.game.common.Dir.E;
import static net.capps.word.game.common.Dir.S;


/**
 * Created by charlescapps on 1/13/15.
 */
public class DefaultGameGenerator implements GameGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultGameGenerator.class);

    private final DictionaryTrie TRIE;

    public DefaultGameGenerator() {
        this.TRIE = Preconditions.checkNotNull(DictionaryTrie.getInstance());
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
            Set<Placement> validPlacements = generateValidPlacements(tileSet, maxWordSize);
            if (validPlacements.isEmpty()) {
                throw new IllegalStateException("Could not find any valid placements!!");
            }
            Placement chosen = RandomUtil.pickRandomElementFromSet(validPlacements);
            LOG.info("Placing word: " + chosen);
            tileSet.placeWord(chosen);
        }

        return tileSet;
    }

    private Placement generateFirstPlacement(TileSet tileSet, int maxWordSize) {
        final int N = tileSet.N;
        final String word = DictionaryWordPicker.getInstance().getRandomWordEqualProbabilityByLength(maxWordSize);
        final int len = word.length();
        final Dir dir = Dir.randomPlayDir();

        int minStartPos = Math.max(0, N / 2 - len + 1);
        int maxStartPos = Math.min(N / 2, N - len);

        int startPos = RandomUtil.randomInt(minStartPos, maxStartPos);
        Pos pos = dir == S ? Pos.of(startPos, N / 2, N) : Pos.of(N / 2, startPos, N);

        return new Placement(word, pos, dir);
    }

    private Set<Placement> generateValidPlacements(TileSet tileSet, int maxWordSize) {
        final int N = tileSet.N;

        Set<Placement> placements = Sets.newHashSet();

        for (int r = 0; r < N - 1; r++) {
            for (int c = 0; c < N - 1; c++) {
                Pos p = Pos.of(r, c, N);
                if (!tileSet.isOccupied(p)) {
                    placements.addAll(getValidPlacementsFromUnoccupiedStart(tileSet, p, S, maxWordSize));
                    placements.addAll(getValidPlacementsFromUnoccupiedStart(tileSet, p, E, maxWordSize));
                }
            }
        }

        return placements;
    }

    private Set<Placement> getValidPlacementsFromUnoccupiedStart(TileSet tileSet, Pos start, Dir dir, int maxWordSize) {
        if (tileSet.isOccupied(start)) {
            throw new IllegalStateException();
        }

        Optional<Pos> firstOccupiedOrAdjacent = tileSet.getFirstOccupiedOrAdjacent(start, dir, maxWordSize);

        if (!firstOccupiedOrAdjacent.isPresent()) {
            return Placement.EMPTY_SET;
        }

        Pos occOrAdj = firstOccupiedOrAdjacent.get();

        // If the tile in the reverse direction is occupied, we must consider our play including all occupied tiles
        // in that direction.
        Pos previous = start.go(dir.negate());
        if (tileSet.isOccupied(previous)) {
            start = tileSet.getEndOfOccupied(previous, dir.negate());
        }

        Set<Placement> placements = Sets.newHashSet();
        final int diff = occOrAdj.minus(start);

        int maxSearched = -1;

        // Try words of valid sizes
        for (int i = diff; i < maxWordSize; i++) {
            if (i <= maxSearched) {
                continue;
            }
            Pos p = start.go(dir, i);
            if (!tileSet.isValid(p)) {
                break;
            }

            Pos wordEndPos = tileSet.getEndOfOccupiedFromOccupiedOrAdj(p, dir);
            int totalDiff = wordEndPos.minus(start);

            maxSearched = Math.max(maxSearched, totalDiff);

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
                if (!tileSet.isValidPlacement(placement).isPresent()) {
                    placements.add(placement);
                    break; // Only add one possible play per length to reduce computation
                }
            }
        }

        return placements;
    }

}
