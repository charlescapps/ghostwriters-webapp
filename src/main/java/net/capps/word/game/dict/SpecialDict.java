package net.capps.word.game.dict;

import net.capps.word.game.gen.GameGenerator;
import net.capps.word.game.gen.SpecialDictGameGenerator;

/**
 * Created by charlescapps on 5/10/15.
 *
 * Represents dictionaries that can be used in the Ghostwriters game.
 */
public enum SpecialDict {
    POE(DictType.POE, 1),
    LOVECRAFT(DictType.LOVECRAFT, 1),
    MYTHOS(DictType.MYTHOS, 1);

    private final DictType primaryDict;
    private final int tokenCost;

    SpecialDict(DictType primaryDict, int tokenCost) {
        this.primaryDict = primaryDict;
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

    public DictType getDictType() {
        return primaryDict;
    }

    public DictType getDictForWord(String word) {
        if (primaryDict.getDictionary().contains(word)) {
            return primaryDict;
        }
        return null;
    }

    public static SpecialDict ofDictType(DictType dictType) {
        for (SpecialDict specialDict: SpecialDict.values()) {
            if (specialDict.getDictType() == dictType) {
                return specialDict;
            }
        }
        return null;
    }
}
