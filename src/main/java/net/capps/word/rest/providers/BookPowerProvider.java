package net.capps.word.rest.providers;

import net.capps.word.rest.models.UserModel;

/**
 * Created by charlescapps on 8/9/15.
 */
public class BookPowerProvider {
    private static final BookPowerProvider INSTANCE = new BookPowerProvider();

    public static BookPowerProvider getInstance() {
        return INSTANCE;
    }

    private static final int FIVE_PERCENT_BONUS_THRESHOLD = 10;
    private static final int TEN_PERCENT_BONUS_THRESHOLD = 100;
    private static final int FIFTEEN_PERCENT_BONUS_THRESHOLD = 250;
    private static final int TWENTY_PERCENT_BONUS_THRESHOLD = 500;

    public int adjustRatingIncrease(int originalIncrease, UserModel player) {
        if (player == null || player.getTokens() == null) {
            return originalIncrease;
        }

        if (Boolean.TRUE.equals(player.getInfiniteBooks())) {
            return (int) Math.ceil(1.25d * originalIncrease);
        }

        final int TOKENS = player.getTokens();

        if (TOKENS >= TWENTY_PERCENT_BONUS_THRESHOLD) {
            return (int) Math.ceil(1.2d * originalIncrease);
        } else if (TOKENS >= FIFTEEN_PERCENT_BONUS_THRESHOLD) {
            return (int) Math.ceil(1.15d * originalIncrease);
        } else if (TOKENS >= TEN_PERCENT_BONUS_THRESHOLD) {
            return (int) Math.ceil(1.1d * originalIncrease);
        } else if (TOKENS >= FIVE_PERCENT_BONUS_THRESHOLD) {
            return (int) Math.ceil(1.05d * originalIncrease);
        }

        return originalIncrease;
    }

}
