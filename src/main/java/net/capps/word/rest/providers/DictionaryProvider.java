package net.capps.word.rest.providers;

import net.capps.word.game.dict.SpecialDict;
import net.capps.word.rest.models.ErrorModel;
import net.capps.word.util.ErrorOrResult;

import static java.lang.String.format;

/**
 * Created by charlescapps on 6/3/15.
 */
public class DictionaryProvider {
    private static final DictionaryProvider INSTANCE = new DictionaryProvider();

    public static DictionaryProvider getInstance() {
        return INSTANCE;
    }

    private DictionaryProvider() { }

    public ErrorOrResult<SpecialDict> validateSpecialDictionary(String dictName) {
        SpecialDict specialDict;
        try {
            specialDict = SpecialDict.valueOf(dictName);
        } catch (Exception e) {
            return ErrorOrResult.ofError(new ErrorModel(format("Invalid dictionary name '%s'", dictName)));
        }

        return ErrorOrResult.ofResult(specialDict);
    }
}
