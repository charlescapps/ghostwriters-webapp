package net.capps.word.ranks;

import net.capps.word.rest.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.lang.String.format;

/**
 * Created by charlescapps on 8/1/15.
 */
public class UserRanks {
    private static final UserRanks INSTANCE = new UserRanks();
    private static final Logger LOG = LoggerFactory.getLogger(UserRanks.class);

    public static UserRanks getInstance() {
        return INSTANCE;
    }

    private final Map<Integer, UserWithRating> usersById = new HashMap<>();
    private final TreeSet<UserWithRating> rankTree = new TreeSet<>();

    public synchronized void buildRanks(Iterable<UserWithRating> rankedUsers) {
        usersById.clear();
        rankTree.clear();

        final long START = System.currentTimeMillis();
        for (UserWithRating userWithRating : rankedUsers) {
            insertRankedUser(userWithRating);
        }
        final long END = System.currentTimeMillis();
        LOG.info("Built {} ranks in {} milliseconds", rankTree.size(), END - START);
    }

    public synchronized void insertRankedUser(UserWithRating userWithRating) {
        usersById.put(userWithRating.getUserId(), userWithRating);
        rankTree.add(userWithRating);
    }

    public synchronized void updateRankedUser(UserWithRating userWithRating) {
        UserWithRating oldUserWithRating = usersById.get(userWithRating.getUserId());
        usersById.put(userWithRating.getUserId(), userWithRating);
        rankTree.remove(oldUserWithRating);
        rankTree.add(userWithRating);
    }

    public UserWithRating getFirstPlaceUser() {
        return rankTree.first();
    }

    public UserWithRating getLastPlaceUser() {
        return rankTree.last();
    }

    public int getHighestRank() {
        return rankTree.size();
    }

    public synchronized List<UserWithRanking> getUsersWithRankAround(final int centerUserId, int limit) {
        UserWithRating userWithRating = usersById.get(centerUserId);
        if (userWithRating == null) {
            throw new IllegalStateException("User with ID " + centerUserId + " is not in the usersById map in UserRanks.");
        }
        if (!rankTree.contains(userWithRating)) {
            throw new IllegalStateException(
                    format("Error - user is not contained in the TreeSet for ranks: %s", userWithRating));
        }

        List<UserWithRanking> previousRanks = getPreviousRankedUsers(userWithRating, limit);
        int centerRank = previousRanks.isEmpty() ?
                rankTree.headSet(userWithRating, true).size() :
                previousRanks.get(previousRanks.size() - 1).getRank() + 1;

        List<UserWithRanking> nextRanks = getNextRankedUsers(userWithRating, limit, centerRank + 1);

        List<UserWithRanking> resultRanks = new ArrayList<>(previousRanks.size() + 1 + nextRanks.size());
        resultRanks.addAll(previousRanks);
        resultRanks.add(new UserWithRanking(userWithRating.getUserId(), userWithRating.getRating(), centerRank));
        resultRanks.addAll(nextRanks);
        return resultRanks;
    }

    private List<UserWithRanking> getPreviousRankedUsers(UserWithRating centerUser, int limit) {
        NavigableSet<UserWithRating> previousRanks = rankTree.headSet(centerUser, false);
        Iterator<UserWithRating> previousRanksDesc = previousRanks.descendingIterator();
        List<UserWithRanking> results = new ArrayList<>();
        int currentRank = -1;
        for (int i = 0; i < limit; ++i) {
            if (!previousRanksDesc.hasNext()) {
                break;
            }
            UserWithRating user = previousRanksDesc.next();

            if (currentRank == -1) {
                currentRank = rankTree.headSet(user, true).size();
            } else {
                --currentRank;
            }

            results.add(new UserWithRanking(user.getUserId(), user.getRating(), currentRank));
        }
        Collections.reverse(results);
        return results;
    }

    private List<UserWithRanking> getNextRankedUsers(UserWithRating centerUser, int limit, final int startRank) {
        NavigableSet<UserWithRating> nextRanks = rankTree.tailSet(centerUser, false);
        Iterator<UserWithRating> nextRanksAsc = nextRanks.iterator();
        List<UserWithRanking> results = new ArrayList<>();
        int currentRank = startRank;
        for (int i = 0; i < limit; ++i) {
            if (!nextRanksAsc.hasNext()) {
                break;
            }
            UserWithRating user = nextRanksAsc.next();
            results.add(new UserWithRanking(user.getUserId(), user.getRating(), currentRank));
            ++currentRank;
        }
        return results;
    }

}
