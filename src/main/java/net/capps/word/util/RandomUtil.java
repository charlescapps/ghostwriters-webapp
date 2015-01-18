package net.capps.word.util;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Random;

/**
 * Created by charlescapps on 1/17/15.
 */
public class RandomUtil {
    private static final Random RANDOM = new Random();

    public static <T> List<T> randomizeList(List<T> input) {
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
        return minInclusive + RANDOM.nextInt(maxInclusive - minInclusive + 1);
    }
}
