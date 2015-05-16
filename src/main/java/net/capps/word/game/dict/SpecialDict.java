package net.capps.word.game.dict;

import net.capps.word.game.gen.GameGenerator;
import net.capps.word.game.gen.SpecialDictGameGenerator;

/**
 * Created by charlescapps on 5/10/15.
 *
 * Represents dictionaries that can be used in the Ghostwriters game.
 */
public enum SpecialDict {
    POE(DictType.POE, null, 1),
    LOVECRAFT(DictType.LOVECRAFT, null, 1),
    MYTHOS(DictType.MYTHOS, DictType.LOVECRAFT, 2);

    private final DictType primaryDict;
    private final DictType secondaryDict;
    private final int tokenCost;

    SpecialDict(DictType primaryDict, DictType secondaryDict, int tokenCost) {
        this.primaryDict = primaryDict;
        this.secondaryDict = secondaryDict;
        this.tokenCost = tokenCost;
    }

    public GameGenerator getGameGenerator() {
        switch (this) {
            case POE:
            case LOVECRAFT:
            case MYTHOS:
                return new SpecialDictGameGenerator(this);
        }
        throw new IllegalStateException("Invalid GameDict enum given...");
    }

    public int getTokenCost() {
        return tokenCost;
    }

    public DictType getPrimaryDict() {
        return primaryDict;
    }

    public DictType getSecondaryDict() {
        return secondaryDict;
    }

    public DictType getDictForWord(String word) {
        if (primaryDict.getDictionarySet().contains(word)) {
            return primaryDict;
        }
        if (secondaryDict != null && secondaryDict.getDictionarySet().contains(word)) {
            return secondaryDict;
        }
        return null;
    }
}
