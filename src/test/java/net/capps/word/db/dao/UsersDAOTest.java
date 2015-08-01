package net.capps.word.db.dao;

import net.capps.word.db.WordDbManager;
import net.capps.word.exceptions.WordDbException;
import net.capps.word.rest.models.UserModel;
import net.capps.word.rest.providers.RatingsProvider;
import net.capps.word.rest.providers.UserRecordChange;
import net.capps.word.rest.providers.UsersProvider;
import net.capps.word.util.RandomUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Created by charlescapps on 7/29/15.
 */
public class UsersDAOTest {
    private static final int NUM_USERS = 10000;

    private static final int MIN_RATING = 1000;
    private static final int MAX_RATING = 10000;

    private static final UsersDAO usersDAO = UsersDAO.getInstance();
    private static final RatingsProvider ratingProvider = RatingsProvider.getInstance();

    @Test
    public void testCreateManyUsers() throws Exception {

        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            for (int i = 0; i < NUM_USERS; ++i) {
                createUserWithRandomRating(i, usersDAO, dbConn);
            }
        }
    }

    @Test
    public void testQueryLeaderboardManyTimesAndPrintDurations() throws Exception {
        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            int maxUserId = usersDAO.getMaximumId(dbConn);
            Assert.assertTrue("Expected more than 3 users", maxUserId > 3);

            System.out.println("Num users in database:" + maxUserId);

            final int numTimesToQueryLeaderboard = 100;
            System.out.printf("Starting to query leaderboard %d times...\n", numTimesToQueryLeaderboard);
            System.out.println("Printing the times in milliseconds.");

            final long START = System.currentTimeMillis();

            for (int i = 0; i < numTimesToQueryLeaderboard; ++i) {
                final int randomId = RandomUtil.randomInt(1, maxUserId);
                Optional<UserModel> optUser = usersDAO.getUserById(dbConn, randomId);
                Assert.assertTrue("Expected user with id " + randomId + " to exist.", optUser.isPresent());

                queryLeaderboard(dbConn, optUser.get());
            }

            final long END = System.currentTimeMillis();

            System.out.println("Total time to query leaderboard many times (seconds): " +
                    TimeUnit.MILLISECONDS.toSeconds(END - START));
        }
    }

    private void queryLeaderboard(Connection dbConn, UserModel userModel) throws Exception {
        final long START = System.currentTimeMillis();
        ratingProvider.getUsersWithRankAroundMe(dbConn, userModel, 50);
        final long END = System.currentTimeMillis();

        System.out.println(END - START);
    }


    private UserModel createUserWithRandomRating(int index, UsersDAO usersDAO, Connection dbConn) throws Exception {
        final String username = "User_" + index + "_" + RandomStringUtils.randomAlphanumeric(4);
        UserModel inputUser = new UserModel(null, username, null, null, null, false);
        UserModel createdUser = usersDAO.insertNewUser(dbConn, inputUser);

        final int rating = RandomUtil.randomInt(MIN_RATING, MAX_RATING);
        usersDAO.updateUserRating(dbConn, createdUser.getId(), rating, UserRecordChange.INCREASE_WINS);

        return createdUser;
    }
}
