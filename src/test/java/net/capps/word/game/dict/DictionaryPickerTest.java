package net.capps.word.game.dict;

import net.capps.word.heroku.SetupHelper;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by charlescapps on 1/18/15.
 */
public class DictionaryPickerTest {
    @BeforeClass
    public static void setup() throws IOException {
        SetupHelper.getInstance().initDictionaryDataStructures();
    }

    @Test
    public void testGenerateRandomWordsOfLen() {

    }
}
