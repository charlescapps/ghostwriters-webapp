package net.capps.word.game.dict.common;

import net.capps.word.game.dict.DictType;
import net.capps.word.game.dict.SpecialDict;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by charlescapps on 6/27/15.
 */
public class DictHelpers {
    public static boolean isPrefix(String word, DictType dictType) {
        return dictType.getDictionaryTrie().isPrefix(word);
    }

    public static boolean isWord(String word, DictType dictType) {
        return dictType.getDictionary().contains(word);
    }

    public static DictType[] selectDictionaryOrderForMove(SpecialDict specialDict, float probabilityToSelectWordFromSpecialDict) {
        if (specialDict == null) {
            return new DictType[] { DictType.ENGLISH_WORDS };
        }

        final Random random = ThreadLocalRandom.current();

        // Select the dictionary to use for this move
        float coinFlipToUseSpecialDict = random.nextFloat();
        if (coinFlipToUseSpecialDict < probabilityToSelectWordFromSpecialDict) {
            return new DictType[] { specialDict.getDictType(), DictType.ENGLISH_WORDS };
        }

        return new DictType[] { DictType.ENGLISH_WORDS, specialDict.getDictType() };
    }

}
