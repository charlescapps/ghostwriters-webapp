package net.capps.word.game.dict;

import net.capps.word.heroku.SetupHelper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by charlescapps on 1/18/15.
 */
public class DictionaryWordPickerTest {
    @BeforeClass
    public static void setup() throws IOException {
        SetupHelper.getInstance().initDictionaryDataStructures();
    }

    @Test
    public void testGenerateRandomWordsOfLen() {
        for (int maxLen = 2; maxLen <= 15; maxLen++) {
            for (int i = 0; i < 100; i++) {
                String word = DictionaryWordPicker.getInstance().getRandomWordEqualProbabilityByLength(maxLen);
                Assert.assertTrue(String.format("Len of word %s of %d exceeds max len %d", word, word.length(), maxLen),
                       word.length() <= maxLen);
            }
        }
    }
}
