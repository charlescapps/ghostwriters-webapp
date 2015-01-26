package net.capps.word.game.gen;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.capps.word.game.common.BoardSize;
import net.capps.word.game.common.Pos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by charlescapps on 1/25/15.
 */
public class PositionLists {
    private static final PositionLists INSTANCE = new PositionLists();
    private static final Logger LOG = LoggerFactory.getLogger(PositionLists.class);

    private ImmutableMap<Integer, ImmutableList<Pos>> POSITION_LISTS;

    public static PositionLists getInstance() {
        return INSTANCE;
    }

    public void load() {
        Preconditions.checkArgument(POSITION_LISTS == null, "Cannot call PositionLists.load() twice.");
        LOG.info("Building Position lists...");

        ImmutableMap.Builder<Integer, ImmutableList<Pos>> builder = ImmutableMap.builder();

        for (int N = BoardSize.TALL.getN(); N <= BoardSize.VENTI.getN(); N++) {
            builder.put(N, generatePositions(N));
        }

        POSITION_LISTS = builder.build();
        LOG.info("SUCCESS - Finished building position lists.");
    }

    public ImmutableList<Pos> getPositionList(int N) {
        return POSITION_LISTS.get(N);
    }

    private ImmutableList<Pos> generatePositions(final int N) {
        final int numPositions = N * N;
        ImmutableList.Builder<Pos> builder = ImmutableList.builder();

        for (int i = 0; i < numPositions; i++) {
            int r = i / N;
            int c = i % N;
            builder.add(Pos.of(r, c));
        }
        ImmutableList<Pos> result = builder.build();
        if (result.size() != numPositions) {
            throw new IllegalStateException();
        }
        return result;
    }
}
