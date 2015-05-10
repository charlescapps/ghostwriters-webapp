package net.capps.word.game.dict;

import com.google.common.collect.Lists;
import net.capps.word.game.common.BoardSize;
import net.capps.word.heroku.SetupHelper;
import net.capps.word.util.DateUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;

/**
 * Created by charlescapps on 1/17/15.
 */
public class DictionaryTrieTest {
    private static final Logger LOG = LoggerFactory.getLogger(DictionaryTrieTest.class);

    @BeforeClass
    public static void setup() throws Exception {
        SetupHelper.getInstance().initDictionaryDataStructures();
    }

    @Test
    public void testGetWord() {
        final DictionarySet SET = Dictionaries.getEnglishDictSet();
        final DictionaryTrie TRIE = Dictionaries.getEnglishDictTrie();
    }

    @Test
    public void testTrieContainsMethod() {
        final DictionarySet SET = Dictionaries.getEnglishDictSet();
        final DictionaryTrie TRIE = Dictionaries.getEnglishDictTrie();

        LOG.info("Verifying that the Trie contains every word...");
        for (String word: SET.getWords()) {
            Assert.assertTrue("Expect Trie to contan every word added to dictionary", TRIE.contains(word));
        }

        for (int i = 0 ; i < 100 ; i++) {
            String random = RandomStringUtils.randomAlphabetic(8).toUpperCase();
            if (SET.contains(random)) {
                Assert.assertTrue(TRIE.contains(random));
            } else {
                Assert.assertTrue(!TRIE.contains(random));
            }
        }
        LOG.info("SUCCESS - Trie contains() method is valid");
    }

    @Test
    public void testContainsPerformance() {
        final DictionarySet SET = Dictionaries.getEnglishDictSet();
        final DictionaryTrie TRIE = Dictionaries.getEnglishDictTrie();
        Set<String> words = SET.getWords();

        long START = System.currentTimeMillis();
        for (String word: words) {
            boolean contains = SET.contains(word);
        }
        long END = System.currentTimeMillis();
        LOG.info("Checking words are contained in Set took: {}", DateUtil.getDurationPrettyMillis(END - START));

        START = System.currentTimeMillis();
        for (String word: words) {
            boolean contains = TRIE.contains(word);
        }

        END = System.currentTimeMillis();
        LOG.info("Checking words are contained in Trie took: {}", DateUtil.getDurationPrettyMillis(END - START));
    }

    @Test
    public void testEnumerateWordsWithOneConstraint() {
        final DictionaryWordSets WORD_SETS = Dictionaries.getEnglishWordSets();
        Set<String> foundWords = new HashSet<>();

        WordConstraint constraint = new WordConstraint((byte) 2, 'C');

        Iterator<String> iter = WORD_SETS.getWordsWithConstraintsInRandomOrder(Lists.newArrayList(constraint), 5);
        while (iter.hasNext()) {
            String word = iter.next();
            foundWords.add(word);
            LOG.info("FOUND: {}", word);
            Assert.assertTrue(format("Expected word %s to have %s", word, constraint), constraint.apply(word));

        }
        LOG.info("Num found: {}", foundWords.size());
    }

    @Test
    public void testEnumerateWordsWithAllSingleConstraints() {
        final DictionaryWordSets WORD_SETS = Dictionaries.getEnglishWordSets();
        int num = 0;

        for (int len = 2; len <= 15; len++) {
            for (int pos = 0; pos < len; pos++) {
                for (char c = 'A'; c <= 'Z'; c++) {
                    WordConstraint wc = new WordConstraint((byte)pos, c);

                    Iterator<String> iterator = WORD_SETS.getWordsWithConstraintsInRandomOrder(Lists.newArrayList(wc), len);

                    boolean first = true;
                    while (iterator.hasNext()) {
                        ++num;
                        String word = iterator.next();
                        if (first) {
                            first = false;
                            LOG.info("{}: {}", wc, word);
                        }
                        Assert.assertTrue(format("Expected word %s to have %s", word, wc), wc.apply(word));
                    }
                }
            }
        }
        LOG.info("Total num={}", num);
    }

    @Test
    public void testGetRandomWordOfLen() {
        final DictionaryWordSets WORD_SETS = Dictionaries.getEnglishWordSets();

        for (int len = 2; len < BoardSize.VENTI.getN(); ++len) {
            String randomWord = WORD_SETS.getRandomWordOfLen(len);
            LOG.info("Random Word: {}", randomWord);
            Assert.assertEquals(len, randomWord.length());
        }
    }

    @Test
    public void testEnumerateWordsWith2Constraints() {
        final DictionaryWordSets WORD_SETS = Dictionaries.getEnglishWordSets();
        for (int len = 2; len <= 15; len++) {
            for (int pos1 = 0; pos1 < len - 1; pos1++) {
                for (int pos2 = pos1 + 1; pos2 < len; pos2++) {
                    for (char c1 = 'A'; c1 <= 'Z'; c1++) {
                        for (char c2 = 'A'; c2 <= 'Z'; c2++) {
                            WordConstraint wc1 = new WordConstraint((byte)pos1, c1);
                            WordConstraint wc2 = new WordConstraint((byte)pos2, c2);

                            List<WordConstraint> wcs = Lists.newArrayList(wc1, wc2);
                            Iterator<String> iter = WORD_SETS.getWordsWithConstraintsInRandomOrder(wcs, len);

                            boolean first = true;
                            while (iter.hasNext()) {
                                String word = iter.next();

                                if (first) {
                                    first = false;
                                    LOG.info("{} and {}: {}", wc1, wc2, word);
                                }
                                Assert.assertTrue(format("Expected word %s to have %s and %s", word, wc1, wc2),
                                        wc1.apply(word) && wc2.apply(word));

                            }

                        }
                    }
                }
            }
        }
    }
}
