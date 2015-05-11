package net.capps.word.game.dict;

import net.capps.word.game.gen.ExtraDictGameGenerator;
import net.capps.word.game.gen.GameGenerator;
import net.capps.word.game.gen.SpecialDictGameGenerator;

/**
 * Created by charlescapps on 5/10/15.
 *
 * Represents dictionaries that can be used in the Ghostwriters game.
 */
public enum GameDict {
    POE(DictType.POE, null),
    LOVECRAFT(DictType.LOVECRAFT, null),
    MYTHOS(DictType.MYTHOS, DictType.LOVECRAFT);

    private final DictType primaryDict;
    private final DictType secondaryDict;

    GameDict(DictType primaryDict, DictType secondaryDict) {
        this.primaryDict = primaryDict;
        this.secondaryDict = secondaryDict;
    }

    public GameGenerator getGameGenerator() {
        switch (this) {
            case POE:
            case LOVECRAFT:
                return ExtraDictGameGenerator.of(this.primaryDict);
            case MYTHOS:
                return new SpecialDictGameGenerator(primaryDict.getDictionaryWordSets(), secondaryDict.getDictionaryWordSets());
        }
        throw new IllegalStateException("Invalid GameDict enum given...");
    }
}
