package net.capps.word.db.dao;

import com.google.common.collect.Sets;
import net.capps.word.db.WordDbManager;
import net.capps.word.heroku.SetupHelper;
import net.capps.word.ranks.UserWithRating;
import net.capps.word.ranks.UserRanks;
import net.capps.word.rest.models.UserModel;
import net.capps.word.rest.providers.RatingsProvider;
import net.capps.word.rest.providers.UserRecordChange;
import net.capps.word.util.RandomUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by charlescapps on 7/29/15.
 */
public class RankingTest {
    private static final int NUM_USERS = 90000;

    private static final int MIN_RATING = 1000;
    private static final int MAX_RATING = 10000;

    private static final UsersDAO usersDAO = UsersDAO.getInstance();
    private static final RatingsProvider ratingProvider = RatingsProvider.getInstance();
    private static final SetupHelper setupHelper = SetupHelper.getInstance();

    private static final int DEFAULT_RANK_COUNT = 50;

    @Test
    public void testQueryLeaderboardConcurrent() throws Exception {
        setupHelper.initRankDataStructures();

        final int NUM_THREADS = 16;
        final int NUM_TASKS = 200;
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        List<Callable<Void>> callables = new ArrayList<>();

        for (int i = 0; i < NUM_TASKS; ++i) {
            callables.add(() -> {
                doTestQueryLeaderboardAndPrintTotalTime(25, 50);
                return null;
            });
        }

        List<Future<Void>> futures = executor.invokeAll(callables);

        for (Future<Void> future: futures) {
            future.get();
        }
    }

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
        setupHelper.initRankDataStructures();

        final int NUM_USERS_AROUND = 25;

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

                queryLeaderboard(dbConn, optUser.get(), NUM_USERS_AROUND);
            }

            final long END = System.currentTimeMillis();

            System.out.println("Total time to query leaderboard many times (seconds): " +
                    TimeUnit.MILLISECONDS.toSeconds(END - START));
        }
    }

    @Test
    public void testQueryLeaderboardCorrectness() throws Exception {
        setupHelper.initRankDataStructures();

        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            int maxUserId = usersDAO.getMaximumId(dbConn);
            Assert.assertTrue("Expected more than 3 users", maxUserId > 3);

            System.out.println("Num users in database:" + maxUserId);

            final int numTimesToQueryLeaderboard = 100;
            System.out.printf("Starting to query leaderboard %d times and verifying correctness...\n", numTimesToQueryLeaderboard);

            final long START = System.currentTimeMillis();

            for (int i = 0; i < numTimesToQueryLeaderboard; ++i) {
                final int randomId = RandomUtil.randomInt(1, maxUserId);
                Optional<UserModel> optUser = usersDAO.getUserById(dbConn, randomId);
                Assert.assertTrue("Expected user with id " + randomId + " to exist.", optUser.isPresent());

                List<UserModel> results = queryLeaderboard(dbConn, optUser.get(), DEFAULT_RANK_COUNT);
                Assert.assertTrue("Expected ranks to be non-empty", !results.isEmpty());
                assertRanksValid(results);
                assertRanksSequential(results);
                Assert.assertTrue("Expected ranks to contain the center user", results.contains(optUser.get()));

                System.out.println("Rankings:");
                for (UserModel result: results) {
                    System.out.println(result.getRank());
                }
            }

            final long END = System.currentTimeMillis();

            System.out.println("Total time to query leaderboard many times (seconds): " +
                    TimeUnit.MILLISECONDS.toSeconds(END - START));
        }
    }

    @Test
    public void testQueryLeaderboardCorrectnessRank1() throws Exception {
        setupHelper.initRankDataStructures();

        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            int maxUserId = usersDAO.getMaximumId(dbConn);
            Assert.assertTrue("Expected more than 3 users", maxUserId > 3);

            System.out.println("Num users in database:" + maxUserId);

            final long START = System.currentTimeMillis();

            UserWithRating firstPlaceUser = UserRanks.getInstance().getFirstPlaceUser();

            Optional<UserModel> optUser = usersDAO.getUserById(dbConn, firstPlaceUser.getUserId());
            Assert.assertTrue("Expected user with the lowest rank to exist.", optUser.isPresent());

            List<UserModel> results = queryLeaderboard(dbConn, optUser.get(), DEFAULT_RANK_COUNT);
            Assert.assertTrue("Expected ranks to be non-empty", !results.isEmpty());
            Assert.assertTrue("Expected the rank to be 1 for the first result", results.get(0).getRank() == 1);
            assertRanksValid(results);
            assertRanksSequential(results);
            Assert.assertTrue("Expected ranks to contain the center user", results.contains(optUser.get()));

            System.out.println("Rankings:");
            for (UserModel result: results) {
                System.out.println(result.getRank());
            }

            final long END = System.currentTimeMillis();

            System.out.println("Total time to query leaderboard many times (seconds): " +
                    TimeUnit.MILLISECONDS.toSeconds(END - START));
        }
    }

    @Test
    public void testQueryLeaderboardCorrectnessLastRank() throws Exception {
        setupHelper.initRankDataStructures();

        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            int maxUserId = usersDAO.getMaximumId(dbConn);
            Assert.assertTrue("Expected more than 3 users", maxUserId > 3);

            System.out.println("Num users in database:" + maxUserId);

            final long START = System.currentTimeMillis();

            UserWithRating lastPlaceUser = UserRanks.getInstance().getLastPlaceUser();

            Optional<UserModel> optUser = usersDAO.getUserById(dbConn, lastPlaceUser.getUserId());
            Assert.assertTrue("Expected user with the highest rank to exist.", optUser.isPresent());

            List<UserModel> results = queryLeaderboard(dbConn, optUser.get(), DEFAULT_RANK_COUNT);
            Assert.assertTrue("Expected ranks to be non-empty", !results.isEmpty());
            Assert.assertTrue("Expected the rank to be the highest - limit for the first result", results.get(0).getRank() == UserRanks.getInstance().getHighestRank() - 50);
            assertRanksValid(results);
            assertRanksSequential(results);
            Assert.assertTrue("Expected ranks to contain the center user", results.contains(optUser.get()));

            System.out.println("Rankings:");
            for (UserModel result: results) {
                System.out.println(result.getRank());
            }

            final long END = System.currentTimeMillis();

            System.out.println("Total time to query leaderboard many times (seconds): " +
                    TimeUnit.MILLISECONDS.toSeconds(END - START));
        }
    }

    @Test
    public void testUpdateUserRanking() throws Exception {
        setupHelper.initRankDataStructures();

        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            int maxUserId = usersDAO.getMaximumId(dbConn);
            Assert.assertTrue("Expected more than 3 users", maxUserId > 3);

            System.out.println("Num users in database:" + maxUserId);

            // Get 1 rank on either side of a random player until we find 2 players with different ratings.
            UserModel player1;
            List<UserModel> results;
            do {
                int randomUserId = RandomUtil.randomInt(1, maxUserId);
                player1 = usersDAO.getUserById(dbConn, randomUserId).get();
                results = ratingProvider.getUsersWithRankAroundMe(dbConn, player1, 1);
                if (results.size() < 3) {
                    continue;
                }
                Assert.assertTrue("3 results should be returned!", results.size() == 3);
                Assert.assertTrue("Result at index 1 is the center user!", results.get(1).equals(player1));
            } while (results.get(1).getRating().equals(results.get(2).getRating()));

            player1 = results.get(1);
            UserModel player2 = results.get(2);

            System.out.println("player1 rank before: " + player1.getRank());
            System.out.println("player2 rank before: " + player2.getRank());

            // Swap ratings
            int ratingTemp = player1.getRating();
            player1.setRating(player2.getRating());
            player2.setRating(ratingTemp);

            // Update rating in the database
            usersDAO.updateUserRating(dbConn, player1.getId(), player1.getRating(), UserRecordChange.INCREASE_WINS);
            usersDAO.updateUserRating(dbConn, player2.getId(), player2.getRating(), UserRecordChange.INCREASE_LOSSES);

            // Updates in the data structures for ranking
            UserRanks.getInstance().updateRankedUser(new UserWithRating(player1.getId(), player1.getRating()));
            UserRanks.getInstance().updateRankedUser(new UserWithRating(player2.getId(), player2.getRating()));

            // Verify the ranks are swapped
            results = ratingProvider.getUsersWithRankAroundMe(dbConn, player2, 1000);
            Assert.assertTrue("Results should contain player 2", results.contains(player2));
            Assert.assertTrue("Results should contain player 1", results.contains(player1));

            final int player1Index = results.indexOf(player1);
            final int player2Index = results.indexOf(player2);
            Assert.assertTrue("Player1 should be on the right", player2Index < player1Index);
            Assert.assertTrue("Player1 should have a higher rank", results.get(player2Index).getRank() < results.get(player1Index).getRank());
            System.out.println("Player1 rank after: " + results.get(player1Index).getRank());
            System.out.println("Player2 rank after: " + results.get(player2Index).getRank());
        }
    }

    // --------- Private ---------

    private List<UserModel> queryLeaderboard(Connection dbConn, UserModel userModel, int rankCount) throws Exception {
        final long START = System.currentTimeMillis();
        List<UserModel> results = ratingProvider.getUsersWithRankAroundMe(dbConn, userModel, rankCount);
        final long END = System.currentTimeMillis();

        System.out.println(END - START);

        return results;
    }

    private void assertRanksValid(List<UserModel> rankedUsers) {
        Set<Integer> ranks = Sets.newHashSet();
        for (UserModel userModel: rankedUsers) {
            Assert.assertNotNull("Expected all ranks to be defined on returned users.", userModel.getRank());
            Assert.assertTrue("Expected all ranks to be >= 1.", userModel.getRank() >= 1);
            ranks.add(userModel.getRank());
        }

        Assert.assertTrue("Expected all ranks to be unique!", ranks.size() == rankedUsers.size());
    }

    private void assertRanksSequential(List<UserModel> rankedUsers) {
        int prevRank = -1;
        UserModel prevUser = null;
        for (UserModel userModel: rankedUsers) {
            int currentRank = userModel.getRank();
            Assert.assertTrue("Expected ranks to be >= 1", currentRank >= 1);
            if (prevRank != -1) {
                Assert.assertTrue("Expected ranks to be sequential", currentRank == prevRank + 1 );
            }
            if (prevUser != null) {
                UserWithRating pUserWithRating = new UserWithRating(prevUser.getId(), prevUser.getRating());
                UserWithRating cUserWithRating = new UserWithRating(userModel.getId(), userModel.getRating());
                Assert.assertTrue("Expected the next user to be greated with natural comparator",
                        pUserWithRating.compareTo(cUserWithRating) < 0);
            }
            prevRank = currentRank;
            prevUser = userModel;
        }
    }


    private UserModel createUserWithRandomRating(int index, UsersDAO usersDAO, Connection dbConn) throws Exception {
        final String username = "User_" + index + "_" + RandomStringUtils.randomAlphanumeric(4);
        UserModel inputUser = new UserModel(null, username, null, null, null, false);
        UserModel createdUser = usersDAO.insertNewUser(dbConn, inputUser);

        final int rating = RandomUtil.randomInt(MIN_RATING, MAX_RATING);
        usersDAO.updateUserRating(dbConn, createdUser.getId(), rating, UserRecordChange.INCREASE_WINS);
        UserRanks.getInstance().updateRankedUser(new UserWithRating(createdUser.getId(), rating));

        return createdUser;
    }

    private void doTestQueryLeaderboardAndPrintTotalTime(int userCount, int numTimesToQueryLeaderboard) throws Exception {
        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            int maxUserId = usersDAO.getMaximumId(dbConn);
            Assert.assertTrue("Expected more than 3 users", maxUserId > 3);

            final long START = System.currentTimeMillis();

            for (int i = 0; i < numTimesToQueryLeaderboard; ++i) {
                final int randomId = RandomUtil.randomInt(1, maxUserId);
                Optional<UserModel> optUser = usersDAO.getUserById(dbConn, randomId);
                Assert.assertTrue("Expected user with id " + randomId + " to exist.", optUser.isPresent());

                ratingProvider.getUsersWithRankAroundMe(dbConn, optUser.get(), userCount);
            }

            final long END = System.currentTimeMillis();

            System.out.println(END - START);
        }
    }
}
