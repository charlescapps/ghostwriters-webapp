package net.capps.word.ranks;


import javax.validation.constraints.NotNull;

/**
 * Created by charlescapps on 8/1/15.
 */
public class UserWithRanking implements Comparable<UserWithRanking> {
    private final int userId;
    private final int rating;
    private final int rank;

    public UserWithRanking(int userId, int rating, int rank) {
        this.userId = userId;
        this.rating = rating;
        this.rank = rank;
    }

    public int getUserId() {
        return userId;
    }

    public int getRating() {
        return rating;
    }

    public int getRank() {
        return rank;
    }

    @Override
    public int compareTo(@NotNull UserWithRanking o) {
        if (rating != o.rating) {
            return o.rating - rating; // Descending by rating
        }
        return userId - o.userId;     // then Ascending by userId (i.e. date joined Ghostwriters)
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserWithRanking)) {
            return false;
        }
        return userId == ((UserWithRanking)o).userId;
    }

    @Override
    public int hashCode() {
        return userId;
    }
}
