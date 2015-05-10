package net.capps.word.game.gen;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import net.capps.word.game.board.TileSet;
import net.capps.word.game.common.BoardSize;
import net.capps.word.game.common.Placement;
import net.capps.word.game.dict.DictType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by charlescapps on 5/8/15.
 */
public class SpecialGameGenerator implements GameGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(SpecialGameGenerator.class);
    private static final DefaultGameGenerator DEFAULT_GAME_GENERATOR = DefaultGameGenerator.getInstance();
    private static final Map<DictType, SpecialGameGenerator> SPECIAL_GAME_GENERATORS = new HashMap<>();

    private final DefaultGameGenerator specialGameGenerator;

    private SpecialGameGenerator(DictType specialDict) {
        this.specialGameGenerator = new DefaultGameGenerator(specialDict.getDictionaryWordSets());
    }

    public static SpecialGameGenerator of(DictType specialDict) {
        if (SPECIAL_GAME_GENERATORS.get(specialDict) == null) {
            SpecialGameGenerator specialGameGenerator = new SpecialGameGenerator(specialDict);
            SPECIAL_GAME_GENERATORS.put(specialDict, specialGameGenerator);
        }
        return SPECIAL_GAME_GENERATORS.get(specialDict);
    }

    @Override
    public TileSet generateRandomFinishedGame(int N, int numWords, int maxWordSize) {
        Preconditions.checkArgument(numWords > 0, "numWords must be > 0");
        Preconditions.checkArgument(maxWordSize >= 2, "maxWordSize must be at least 2");
        Preconditions.checkArgument(N >= BoardSize.TALL.getN(), "N must be at least 5");

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
            tileSet.placeWord(placement);
        }

        return tileSet;
    }

    @Override
    public Placement generateFirstPlacement(TileSet tileSet, int maxWordSize) {
        return specialGameGenerator.generateFirstPlacement(tileSet, maxWordSize);
    }

    @Override
    public Optional<Placement> findFirstValidPlacementInRandomSearch(TileSet tileSet, int maxWordSize) {
        Optional<Placement> specialWordPlacement = specialGameGenerator.findFirstValidPlacementInRandomSearch(tileSet, maxWordSize);
        if (specialWordPlacement.isPresent()) {
            return specialWordPlacement;
        }
        return DEFAULT_GAME_GENERATOR.findFirstValidPlacementInRandomSearch(tileSet, maxWordSize);
    }
}
