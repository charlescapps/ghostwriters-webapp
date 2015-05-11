package net.capps.word.game.dict;

import net.capps.word.game.dict.iterators.ArrayIterator;
import net.capps.word.game.dict.iterators.EmptyStringIterator;
import net.capps.word.game.dict.sets.WordSets;
import net.capps.word.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/**
 * Created by charlescapps on 5/9/15.
 */
public class DictionaryWordSets {
    private static final Logger LOG = LoggerFactory.getLogger(DictionaryWordSets.class);
    private final Map<Integer, WordSets> wordSetsByLength = new HashMap<>();
    private Set<String> words;

    public DictionaryWordSets() {
    }

    public void loadDictionarySets(Set<String> words) {
        this.words = words;
        LOG.info("Starting to load Dictionary Word Sets...");
        final long START = System.currentTimeMillis();
        Map<Integer, Set<String>> wordsByLength = new HashMap<>();
        for (String word : words) {
            final int len = word.length();
            if (wordsByLength.get(len) == null) {
                wordsByLength.put(len, new HashSet<String>());
            }
            Set<String> wordSet = wordsByLength.get(len);
            wordSet.add(word);
        }

        for (int len: wordsByLength.keySet()) {
            Set<String> wordsOfLen = wordsByLength.get(len);
            wordSetsByLength.put(len, new WordSets(len, wordsOfLen));
        }
        LOG.info("Finished loading Dictionary Word Sets. Took {} seconds",
                TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - START));
    }

    public String getRandomWordOfLen(int len) {
        WordSets wordSets = wordSetsByLength.get(len);
        if (wordSets == null) {
            return null;
        }
        List<String> allWordsOfLen = wordSets.getAllWordsOfLenList();
        int index = ThreadLocalRandom.current().nextInt(allWordsOfLen.size());
        return allWordsOfLen.get(index);
    }

    public String getRandomWordBetweenLengths(final int min, final int max) {
        int totalWords = 0;
        for (int i = min; i <= max; ++i) {
            WordSets wordSets = wordSetsByLength.get(i);
            if (wordSets == null) {
                continue;
            }
            totalWords += wordSets.getAllWordsOfLenList().size();
        }

        int wordChoice = ThreadLocalRandom.current().nextInt(totalWords);

        for (int i = min; i <= max; i++) {
            WordSets wordSets = wordSetsByLength.get(i);
            if (wordSets == null) {
                continue;
            }
            if (wordChoice < wordSets.size()) {
                return wordSets.getAllWordsOfLenList().get(wordChoice);
            }
            wordChoice -= wordSets.size();
        }
        throw new IllegalStateException(format("No words found between length %d and %d !", min, max));
    }

    public Iterator<String> getWordsWithConstraintsInRandomOrder(List<WordConstraint> wcs, int len) {
        WordSets wordSets = wordSetsByLength.get(len);
        if (wordSets == null) {
            return EmptyStringIterator.INSTANCE;
        }

        Collection<String> intersection = wordSets.getIntersection(wcs);
        String[] array = intersection.toArray(new String[intersection.size()]);
        RandomUtil.shuffleInPlace(array);
        return new ArrayIterator<>(array);
    }

    public boolean contains(String word) {
        return words.contains(word);
    }

}
