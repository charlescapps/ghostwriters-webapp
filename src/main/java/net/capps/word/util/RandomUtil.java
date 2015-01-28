package net.capps.word.util;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by charlescapps on 1/17/15.
 */
public class RandomUtil {

    public static <T> T[] shuffleArray(T[] items) {
        T[] copy = Arrays.copyOf(items, items.length);
        shuffleInPlace(copy);
        return copy;
    }

    public static <T> List<T> shuffleList(List<T> items) {
        List<T> copy = Lists.newArrayList(items);
        shuffleInPlace(copy);
        return copy;
    }

    public static <T> void shuffleInPlace(T[] items) {
        final Random random = ThreadLocalRandom.current();
        final int N = items.length;
        for (int i = N - 1; i > 0; --i) {
            int j = random.nextInt(i + 1);
            T tmp = items[i];
            items[i] = items[j];
            items[j] = tmp;
        }
    }

    public static <T> void shuffleInPlace(List<T> items) {
        final Random random = ThreadLocalRandom.current();
        final int N = items.size();
        for (int i = N - 1; i > 0; --i) {
            int j = random.nextInt(i + 1);
            T tmp = items.get(i);
            items.set(i, items.get(j));
            items.set(j, tmp);
        }
    }

    public static int randomInt(int minInclusive, int maxInclusive) {
        final Random RANDOM = ThreadLocalRandom.current();
        return minInclusive + RANDOM.nextInt(maxInclusive - minInclusive + 1);
    }

    public static <T> T pickRandomElementFromSet(Set<T> set) {
        final Random RANDOM = ThreadLocalRandom.current();
        Object[] array = set.toArray();
        int index = RANDOM.nextInt(array.length);
        return (T) array[index]; // Cast should always succeed since we passed in a Set<T>
    }
}
