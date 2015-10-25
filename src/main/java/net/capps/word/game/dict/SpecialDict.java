package net.capps.word.game.dict;

import net.capps.word.game.gen.GameGenerator;
import net.capps.word.game.gen.SpecialDictGameGenerator;

/**
 * Created by charlescapps on 5/10/15.
 *
 * Represents dictionaries that can be used in the Ghostwriters game.
 */
public enum SpecialDict {
    POE(DictType.POE, 0),
    LOVECRAFT(DictType.LOVECRAFT, 0),
    MYTHOS(DictType.MYTHOS, 0);

    private final DictType dictType;
    private final int tokenCost;

    SpecialDict(DictType dictType, int tokenCost) {
        this.dictType = dictType;
        this.tokenCost = tokenCost;
    }

    public GameGenerator getGameGenerator() {
        switch (this) {
            case POE:
            case LOVECRAFT:
            case MYTHOS:
                return new SpecialDictGameGenerator(this, 0.5f);
        }
        throw new IllegalStateException("Invalid GameDict enum given...");
    }

    public int getTokenCost() {
        return tokenCost;
    }

    public DictType getDictType() {
        return dictType;
    }

    public boolean contains(String word) {
        return dictType.getDictionary().contains(word);
    }

    public DictType getDictForWord(String word) {
        if (dictType.getDictionary().contains(word)) {
            return dictType;
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
