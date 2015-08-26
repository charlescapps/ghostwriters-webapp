package net.capps.word.game.gen;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.capps.word.game.board.TileSet;
import net.capps.word.game.common.Dir;
import net.capps.word.game.common.MutPos;
import net.capps.word.game.common.Placement;
import net.capps.word.game.common.Pos;
import net.capps.word.game.dict.DictionaryWordSets;
import net.capps.word.game.dict.SpecialDict;
import net.capps.word.game.dict.WordConstraint;
import net.capps.word.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static net.capps.word.game.common.Dir.S;


/**
 * Created by charlescapps on 1/13/15.
 */
public class SpecialDictGameGenerator implements GameGenerator {
    private static final DefaultGameGenerator DEFAULT_GAME_GENERATOR = DefaultGameGenerator.getInstance();

    private static final Logger LOG = LoggerFactory.getLogger(SpecialDictGameGenerator.class);
    private static final PositionLists POSITION_LISTS = PositionLists.getInstance();
    private final SpecialDict specialDict;
    private final DictionaryWordSets primaryWordSets;
    private final Set<String> usedWords = new HashSet<>();
    private final float probabilityPlayFromSpecialDict;

    public SpecialDictGameGenerator(SpecialDict specialDict, float probabilityPlayFromSpecialDict) {
        this.specialDict = specialDict;
        this.primaryWordSets = specialDict.getDictType().getDictionaryWordSets();
        this.probabilityPlayFromSpecialDict = probabilityPlayFromSpecialDict;
    }

    @Override
    public TileSet generateRandomFinishedGame(int N, int numWords, int maxWordSize) {
        Preconditions.checkArgument(numWords > 0, "numWords must be > 0");
        Preconditions.checkArgument(maxWordSize >= 2, "maxWordSize must be at least 2");
        Preconditions.checkArgument(N >= 5, "N must be at least 5");
        Preconditions.checkArgument(usedWords.isEmpty(), "Cannot re-use a Special2GameGenerator instance!");

        TileSet tileSet = new TileSet(N);

        // Place the first placement
        Placement firstPlacement = generateFirstPlacement(tileSet, maxWordSize);
        tileSet.placeWord(firstPlacement);

        for (int i = 1; i < numWords; i++) {
            Optional<Placement> validPlacementOpt = findFirstValidPlacementInRandomSearch(tileSet, maxWordSize);
            if (!validPlacementOpt.isPresent()) {
                LOG.error("ERROR - couldn't find placement for board:\n{}", tileSet);
                return tileSet;
            }
            Placement placement = validPlacementOpt.get();
            if (primaryWordSets.contains(placement.getWord())) {
                usedWords.add(placement.getWord());
            }
            // LOG.trace("Placing word: " + placement);
            tileSet.placeWord(placement);
        }

        return tileSet;
    }

    @Override
    public Placement generateFirstPlacement(TileSet tileSet, int maxWordSize) {
        final int N = tileSet.N;
        final String word = primaryWordSets.getRandomWordBetweenLengths(2, maxWordSize);
        usedWords.add(word);
        final Dir dir = Dir.randomPlayDir();

        int startPos = 0;
        Pos pos = dir == S ? new Pos(startPos, N / 2) : new Pos(N / 2, startPos);

        return new Placement(word, pos, dir);
    }

    @Override
    public Optional<Placement> findFirstValidPlacementInRandomSearch(TileSet tileSet, int maxWordSize) {
        final float coinFlip = ThreadLocalRandom.current().nextFloat();
        if (coinFlip < probabilityPlayFromSpecialDict) {
            Optional<Placement> placementOpt = findFirstValidPlacementInRandomSearch(tileSet, maxWordSize, primaryWordSets);
            if (placementOpt.isPresent()) {
                return placementOpt;
            }
        }
        return DEFAULT_GAME_GENERATOR.findFirstValidPlacementInRandomSearch(tileSet, maxWordSize);
    }

    public Optional<Placement> findFirstValidPlacementInRandomSearch(TileSet tileSet, int maxWordSize, DictionaryWordSets wordSets) {
        final int N = tileSet.N;

        ImmutableList<Pos> positions = POSITION_LISTS.getPositionList(N);

        // Search the possible start positions in a random order.
        List<Pos> randomOrderPositions = RandomUtil.shuffleList(positions);

        for (Pos p: randomOrderPositions) {
            if (!tileSet.isOccupied(p)) {
                Dir[] randomOrderDirs = RandomUtil.shuffleArray(Dir.VALID_PLAY_DIRS);
                for (Dir dir: randomOrderDirs) {
                    Optional<Placement> optValidPlacement = getFirstValidPlacementFromUnoccupiedStartTile(tileSet, p, dir, maxWordSize, wordSets);
                    if (optValidPlacement.isPresent()) {
                        return optValidPlacement;
                    }
                }
            }
        }

        return Optional.empty();
    }

    private Optional<Placement> getFirstValidPlacementFromUnoccupiedStartTile(TileSet tileSet, Pos start, Dir dir, int maxWordSize, DictionaryWordSets wordSets) {
        // Precondition: the start pos isn't an occupied tile.

        MutPos occOrAdj = tileSet.getFirstOccupiedOrAdjacent(start, dir, maxWordSize);

        if (null == occOrAdj) {
            return Optional.empty();
        }

        // If the tile in the reverse direction is occupied, we must consider our play including all occupied tiles
        // in that direction.
        MutPos mp = tileSet.getEndOfOccupied(start, dir.negate());
        if (!mp.isEquivalent(start)) {
            start = mp.toPos();
        }

        final int diff = occOrAdj.minus(start);

        int maxSearched = -1;

        // Compute possible diffs from the current position to place words at, i.e. possible lengths of words
        List<Integer> diffsToTry = new ArrayList<>();

        mp.go(dir, diff);
        for (int i = diff; i < maxWordSize; i++, mp.go(dir)) {
            if (i <= maxSearched) {
                continue;
            }
            if (!tileSet.isValid(mp)) {
                break;
            }

            MutPos wordEndPos = tileSet.getEndOfOccupied(new MutPos(mp), dir);
            int totalDiff = wordEndPos.minus(start);

            maxSearched = Math.max(maxSearched, totalDiff);
            diffsToTry.add(totalDiff);
        }

        // Try all possible word lengths in a random order.
        RandomUtil.shuffleInPlace(diffsToTry);

        for (int totalDiff : diffsToTry) {

            List<WordConstraint> wcs = new ArrayList<>();

            // Get all constraints from existing tiles.
            MutPos scan = start.toMutPos();
            for (int j = 0; j <= totalDiff; ++j, scan.go(dir)) {
                if (tileSet.isOccupied(scan)) {
                    wcs.add(new WordConstraint(j, tileSet.getLetterAt(scan)));
                }
            }

            // Search the given word sets, ignoring words we've already placed.
            Iterator<String> iter = wordSets.getWordsWithConstraintsInRandomOrder(wcs, totalDiff + 1);

            while (iter.hasNext()) {
                String word = iter.next();
                if (usedWords.contains(word)) {
                    continue;
                }
                Placement placement = new Placement(word, start, dir);
                if (tileSet.isValidPlacement(placement, specialDict)) {
                    return Optional.of(placement);
                }
            }
        }

        return Optional.empty();
    }

}
