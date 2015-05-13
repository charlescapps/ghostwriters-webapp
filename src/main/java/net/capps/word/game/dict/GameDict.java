package net.capps.word.game.dict;

import net.capps.word.game.gen.GameGenerator;
import net.capps.word.game.gen.SpecialDictGameGenerator;

/**
 * Created by charlescapps on 5/10/15.
 *
 * Represents dictionaries that can be used in the Ghostwriters game.
 */
public enum GameDict {
    POE(DictType.POE, null, 1),
    LOVECRAFT(DictType.LOVECRAFT, null, 1),
    MYTHOS(DictType.MYTHOS, DictType.LOVECRAFT, 2);

    private final DictType primaryDict;
    private final DictType secondaryDict;
    private final int tokenCost;

    GameDict(DictType primaryDict, DictType secondaryDict, int tokenCost) {
        this.primaryDict = primaryDict;
        this.secondaryDict = secondaryDict;
        this.tokenCost = tokenCost;
    }

    public GameGenerator getGameGenerator() {
        switch (this) {
            case POE:
            case LOVECRAFT:
            case MYTHOS:
                return new SpecialDictGameGenerator(primaryDict.getDictionaryWordSets(),
                        secondaryDict == null ? null : secondaryDict.getDictionaryWordSets());
        }
        throw new IllegalStateException("Invalid GameDict enum given...");
    }

    public int getTokenCost() {
        return tokenCost;
    }
}
