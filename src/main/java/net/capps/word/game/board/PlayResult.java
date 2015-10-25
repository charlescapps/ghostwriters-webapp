package net.capps.word.game.board;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.capps.word.game.move.MoveType;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

/**
 * Created by charlescapps on 10/24/15.
 * The result of playing a move - only interesting for a {@link MoveType.PLAY_WORD} move
 */
public class PlayResult {
    private final int points;
    private final List<String> specialWordsPlayed;

    // For any move other than a PLAY_WORD move, player earns 0 points and did not play any special words!
    public static final PlayResult NON_PLAY_WORD_RESULT = new PlayResult(0, ImmutableList.of());

    public PlayResult(int points, List<String> specialWordsPlayed) {
        this.points = points;
        this.specialWordsPlayed = Preconditions.checkNotNull(specialWordsPlayed);
    }

    public int getPoints() {
        return points;
    }

    @Nonnull
    public List<String> getSpecialWordsPlayed() {
        return specialWordsPlayed;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PlayResult)) {
            return false;
        }
        PlayResult other = (PlayResult)o;
        return points == other.points && specialWordsPlayed.equals(other.specialWordsPlayed);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(points) ^ Objects.hashCode(specialWordsPlayed);
    }
}
