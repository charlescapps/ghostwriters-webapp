package net.capps.word.util;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by charlescapps on 1/17/15.
 */
public class RandomUtil {

    public static <T> List<T> randomizeList(List<T> input) {
        final Random RANDOM = ThreadLocalRandom.current();

        List<Integer> indices = Lists.newArrayListWithCapacity(input.size());
        for (int i = 0; i < input.size(); i++) {
            indices.add(i);
        }

        List<T> output = Lists.newArrayListWithCapacity(input.size());
        while (!indices.isEmpty()) {
            int i = RANDOM.nextInt(indices.size());
            int chosenIndex = indices.get(i);
            output.add(input.get(chosenIndex));
            indices.remove(i);
        }

        return output;
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
