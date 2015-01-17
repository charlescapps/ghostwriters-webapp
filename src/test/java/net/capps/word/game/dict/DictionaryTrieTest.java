package net.capps.word.game.dict;

import com.google.common.collect.Sets;
import net.capps.word.constants.WordConstants;
import net.capps.word.game.common.GameSize;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by charlescapps on 1/17/15.
 */
public class DictionaryTrieTest {
    private static final Logger LOG = LoggerFactory.getLogger(DictionaryTrieTest.class);

    @BeforeClass
    public static void setup() throws Exception {
        DictionarySet.getInstance().loadDictionary(WordConstants.SCRABBLE_DICT_FILE, 2, GameSize.VENTI.getNumRows());
        Set<String> dict = DictionarySet.getInstance().getWords();

        DictionaryTrie.getInstance().loadDictionary(dict);

    }

    @Test
    public void testCreateRandomWordsOfLength() {
        final DictionaryTrie TRIE = DictionaryTrie.getInstance();
        Set<String> foundWords = Sets.newHashSet();

        for (int len = 2; len <= 15; len++) {
            LOG.info("RANDOM words of length {}", len);
            Iterator<String> it = TRIE.getWordsOfLengthInRandomOrder(len);

            while (it.hasNext()) {
                String word = it.next();
                if (foundWords.contains(word)) {
                    throw new IllegalStateException("A word was iterated twice!");
                }
                foundWords.add(word);
                Assert.assertEquals("Expected the length to be correct!", len, word.length());
                LOG.info(word);
            }
        }

        if (foundWords.size() != DictionarySet.getInstance().getWords().size()) {
            Set<String> missingWords = Sets.newHashSet(DictionarySet.getInstance().getWords());
            missingWords.removeAll(foundWords);
            for (String word: missingWords) {
                LOG.info("MISSING: {}", word);
            }
        }

        Assert.assertEquals("Expected all words to be enumerated!",
                DictionarySet.getInstance().getWords().size(), foundWords.size());
    }

}
