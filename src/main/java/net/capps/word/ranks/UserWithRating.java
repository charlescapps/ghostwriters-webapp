package net.capps.word.ranks;


import com.google.common.base.MoreObjects;

import javax.validation.constraints.NotNull;

/**
 * Created by charlescapps on 8/1/15.
 */
public class UserWithRating implements Comparable<UserWithRating> {
    private final int userId;
    private final int rating;

    public UserWithRating(int userId, int rating) {
        this.userId = userId;
        this.rating = rating;
    }

    public int getUserId() {
        return userId;
    }

    public int getRating() {
        return rating;
    }

    @Override
    public int compareTo(@NotNull UserWithRating o) {
        if (rating != o.rating) {
            return o.rating - rating; // Descending by rating
        }
        return userId - o.userId;     // then Ascending by userId (i.e. date joined Ghostwriters)
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserWithRating)) {
            return false;
        }
        return userId == ((UserWithRating)o).userId;
    }

    @Override
    public int hashCode() {
        return userId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("userId", userId)
                .add("rating", rating)
                .toString();
    }
}
