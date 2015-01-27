package net.capps.word.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.math.IntMath;
import net.capps.word.heroku.SetupHelper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Created by charlescapps on 1/26/15.
 */
public class PermutationUtilTest {
    private static final Logger LOG = LoggerFactory.getLogger(PermutationUtilTest.class);

    @Test
    public void testInitPermutations() {
        PermutationUtil.getInstance().initPermutations();
        for (int N = 2; N <= PermutationUtil.MAX_PRECOMPUTED_N; N++) {
            List<ImmutableList<Integer>> allPerms = PermutationUtil.getInstance().getAllPermutations(N);
            int factorial = IntMath.factorial(N);
            Assert.assertEquals("Expected number of permutations to be correct!", factorial, allPerms.size());
            Set<Integer> rangeSet = getRangeSet(N);
            for (int i = 0; i < allPerms.size(); i++) {
                ImmutableList<Integer> perm = allPerms.get(i);
                Set<Integer> valuesAsSet = Sets.newHashSet(perm);
                Assert.assertEquals(N, perm.size());
                Assert.assertEquals("Expect the permutation to have integers from 0...N-1 as a set", rangeSet, valuesAsSet);
            }
        }
    }

    private Set<Integer> getRangeSet(int N) {
        Set<Integer> range = Sets.newHashSet();
        for (int i = 0; i < N; i++) {
            range.add(i);
        }
        return range;
    }
}
