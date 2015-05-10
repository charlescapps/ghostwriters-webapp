package net.capps.word.game.dict.sets;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.capps.word.game.dict.WordConstraint;

import java.util.*;

/**
 * Created by charlescapps on 5/9/15.
 */
public class WordSets {
    public static final Set<String> EMPTY_STRING_SET = ImmutableSet.of();
    private final int len;
    private final Map<WordConstraint, Set<String>> sets = new HashMap<>();
    private ImmutableList<String> allWordsOfLenList;
    private ImmutableSet<String> allWordsOfLenSet;

    public WordSets(int len, Set<String> words) {
        this.len = len;
        allWordsOfLenSet = ImmutableSet.<String>builder().addAll(words).build();
        allWordsOfLenList = ImmutableList.<String>builder().addAll(words).build();
        for (String word: words) {
            addWord(word);
        }
    }

    public void addWord(String word) {
        Preconditions.checkArgument(word.length() == len);
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            WordConstraint wc = WordConstraint.of(i, c);
            if (sets.get(wc) == null) {
                Set<String> set = new HashSet<>();
                sets.put(wc, set);
            }
            Set<String> set = sets.get(wc);
            set.add(word);
        }
    }

    public Set<String> getSet(WordConstraint wc) {
        Set<String> set = sets.get(wc);
        if (set == null) {
            return EMPTY_STRING_SET;
        }
        return set;
    }

    public int size() {
        return allWordsOfLenList.size();
    }

    public Set<String> getIntersection(List<WordConstraint> wcs) {
        if (wcs.isEmpty()) {
            return allWordsOfLenSet;
        }
        Set<String> currentSet = getSet(wcs.get(0));

        for (int i = 1; i < wcs.size(); i++) {
            if (currentSet.isEmpty()) {
                return currentSet;
            }
            Set<String> set = getSet(wcs.get(i));
            currentSet = Sets.intersection(currentSet, set);
        }
        return currentSet;
    }

    public ImmutableList<String> getAllWordsOfLenList() {
        return allWordsOfLenList;
    }
}
