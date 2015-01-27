package net.capps.word.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by charlescapps on 1/25/15.
 */
public class PermutationUtil {
    private static final PermutationUtil INSTANCE = new PermutationUtil();
    private static final Logger LOG = LoggerFactory.getLogger(PermutationUtil.class);
    public static final int MAX_PRECOMPUTED_N = 10;

    public static PermutationUtil getInstance() {
        return INSTANCE;
    }

    private PermutationUtil() { }

    // ---- Pre-computed permutations.
    // ---- Use immutable lists so it's impossible to accidentally modify a permutation elsewhere. ----
    private Map<Integer, List<ImmutableList<Integer>>> allPermutationsByN = Maps.newHashMap();

    public void initPermutations() {
        LOG.info("Starting to pre-compute permutations");
        final long START = System.currentTimeMillis();
        for (int N = 2; N <= MAX_PRECOMPUTED_N; N++) {
            List<ImmutableList<Integer>> permsByN = generateAllPermutationsOfLength(N);
            allPermutationsByN.put(N, permsByN);
        }
        final long END = System.currentTimeMillis();

        LOG.info("Success - computed permutations in {}", DateUtil.getDurationPrettyMillis(END - START));
    }

    public ImmutableList<Integer> getRandomPermutation(int N) {
        if (2 <= N && N <= MAX_PRECOMPUTED_N) {
            List<ImmutableList<Integer>> precomputedPerms = allPermutationsByN.get(N);
            int index = ThreadLocalRandom.current().nextInt(precomputedPerms.size());
            return precomputedPerms.get(index);
        }
        throw new IllegalStateException("Currently only supports permutations of size at most " + MAX_PRECOMPUTED_N);
    }

    public <T> Iterator<T> iterateInRandomOrder(List<T> values) {
        final int N = values.size();
        if (N <= 1) {
            return values.iterator();
        }
        if (N <= 2 && N <= MAX_PRECOMPUTED_N) {
            ImmutableList<Integer> permutation = getRandomPermutation(N);
            return new RandomOrderIterator<>(permutation, values);
        }
        return RandomUtil.randomizeList(values).iterator();
    }

    public List<ImmutableList<Integer>> getAllPermutations(int N) {
        if (2 <= N && N <= MAX_PRECOMPUTED_N) {
           return allPermutationsByN.get(N);
        }
        throw new IllegalStateException("Currently only supports permutations of size at most " + MAX_PRECOMPUTED_N);

    }


    // ------------- PRIVATE ----------

    private static List<ImmutableList<Integer>> generateAllPermutationsOfLength(int N) {
        List<ImmutableList<Integer>> allPermutationsOfN = Lists.newArrayList();
        generatePerms(allPermutationsOfN, Lists.<Integer>newArrayList(), N);
        return allPermutationsOfN;
    }

    private static void generatePerms(List<ImmutableList<Integer>> perms, List<Integer> listSoFar, int N) {
        if (listSoFar.size() == N) {
            ImmutableList<Integer> perm = ImmutableList.copyOf(listSoFar);
            perms.add(perm);
            return;
        }

        for (int i = 0; i < N; i++) {
            if (!listSoFar.contains(i)) {
                List<Integer> copy = Lists.newArrayList(listSoFar);
                copy.add(i);
                generatePerms(perms, copy, N);
            }
        }

    }

}
