package net.capps.word.db.dao;

import com.google.common.base.Strings;
import net.capps.word.db.WordDbManager;
import net.capps.word.exceptions.ConflictException;
import net.capps.word.exceptions.WordDbException;
import net.capps.word.game.common.GameResult;
import net.capps.word.game.ranking.EloRankingComputer;
import net.capps.word.rest.models.UserModel;
import net.capps.word.rest.providers.UserRecordChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Created by charlescapps on 12/26/14.
 */
public class UsersDAO {
    public static final int MAX_TOKENS = 10;

    private static final Logger LOG = LoggerFactory.getLogger(UsersDAO.class);
    private static final WordDbManager WORD_DB_MANAGER = WordDbManager.getInstance();

    private static final String INSERT_USER_QUERY =
            "INSERT INTO word_users (username, email, device_id, date_joined, is_system_user, rating) " +
                    "VALUES (?, ?, ?, ?, ?, ?);";

    private static final String UPDATE_USER_PASSWORD =
            "UPDATE word_users SET (hashpass, salt) = (?, ?) WHERE id = ?";

    private static final String IS_USER_PASSWORD_DEFINED =
            "SELECT (hashpass IS NOT NULL) AS is_pass_defined FROM word_users WHERE id = ?";

    private static final String UPDATE_USER_RATING_WIN =
            "UPDATE word_users SET (rating, wins) = (?, wins + 1) WHERE id = ?";

    private static final String UPDATE_USER_RATING_LOSE =
            "UPDATE word_users SET (rating, losses) = (?, losses + 1) WHERE id = ?";

    private static final String UPDATE_USER_RATING_TIE =
            "UPDATE word_users SET (rating, ties) = (?, ties + 1) WHERE id = ?";

    private static final String GET_USER_BY_ID_QUERY =
            "SELECT * FROM word_users WHERE id = ?;";

    private static final String GET_USER_BY_USERNAME_QUERY =
            "SELECT * FROM word_users WHERE username = ?;";

    private static final String GET_USER_BY_DEVICE_ID_QUERY =
            "SELECT * FROM word_users WHERE device_id = ?;";

    private static final String GET_USER_BY_EMAIL_QUERY =
            "SELECT * FROM word_users WHERE email = ?;";

    private static final String GET_USER_CASE_INSENSITIVE =
            "SELECT * FROM word_users WHERE lower(username) = lower(?);";

    private static final String PREFIX_SEARCH_QUERY =
            "SELECT * FROM word_users WHERE lower(username) LIKE (lower(?) || '%') ORDER BY username ASC LIMIT ?";

    private static final String SUBSTRING_SEARCH_QUERY =
            "SELECT * FROM word_users WHERE strpos(lower(username), lower(?)) > 0 ORDER BY username ASC LIMIT ?";

    // Get users by ratings
    private static final String GET_USERS_WITH_RATING_GEQ =
            "SELECT * FROM word_users WHERE rating >= ? AND id != ? ORDER BY rating ASC LIMIT ?";

    private static final String GET_USERS_WITH_RATING_LT =
            "SELECT * FROM word_users WHERE rating < ? ORDER BY rating DESC LIMIT ?";

    // User ranking based on rating
    public static final String CREATE_RANKING_VIEW =
            "CREATE OR REPLACE VIEW word_user_ranks AS " +
                    "SELECT word_users.*, row_number() OVER (ORDER BY rating DESC) AS word_rank " +
                    "FROM word_users;";

    // Get users with ranking around a given user
    private static final String GET_USER_WITH_RANK =
            "SELECT * FROM word_user_ranks WHERE id = ?;";

    private static final String GET_USERS_WITH_RANK_LT_BY_RATING =
            "SELECT * FROM word_user_ranks WHERE word_rank < ? ORDER BY word_rank DESC LIMIT ?";

    private static final String GET_USERS_WITH_RANK_GT_BY_RATING =
            "SELECT * FROM word_user_ranks WHERE word_rank > ? ORDER BY word_rank ASC LIMIT ?";

    private static final String GET_BEST_RANKED_USERS =
            "SELECT * FROM word_user_ranks ORDER BY word_rank ASC LIMIT ?";

    // Return games such that either
    // (1) game is IN_PROGRESS and it's my turn, or
    // (2) game is OFFERED and I'm the person who started the game and haven't played the first turn yet.
    private static final String GET_NUM_GAMES_MY_TURN =
            "SELECT COUNT(*) AS count FROM word_games " +
                    "WHERE (player1 = ? AND player1_turn = TRUE OR player2 = ? AND player1_turn = FALSE) AND game_result = ? OR " +
                    "player1 = ? AND player1_turn = TRUE AND game_result = ?";

    private static final String GET_NUM_GAMES_OFFERED_TO_ME =
            "SELECT COUNT(*) AS count FROM word_games " +
                    "WHERE (player2 = ? AND player1_turn = FALSE) AND game_result = ?";

    // ---------- In-app purchases --------
    private static final String INCREASE_TOKENS_FROM_PURCHASE =
            "UPDATE word_users SET tokens = tokens + ? WHERE id = ?;";

    private static final String INCREMENT_TOKENS =
            "UPDATE word_users SET tokens = GREATEST(tokens, LEAST(" + MAX_TOKENS + ", tokens + 1)) WHERE id >= ? AND id < ?;";

    private static final String SPEND_TOKENS =
            "UPDATE word_users SET tokens = GREATEST(0, tokens - ?) WHERE id = ?;";

    private static final String GRANT_INFINITE_BOOKS =
            "UPDATE word_users SET (tokens, infinite_books) = (999999, TRUE) WHERE id = ?;";

    private static final UsersDAO INSTANCE = new UsersDAO();

    public static UsersDAO getInstance() {
        return INSTANCE;
    }

    public static enum SearchType {
        PREFIX(PREFIX_SEARCH_QUERY), SUBSTRING(SUBSTRING_SEARCH_QUERY);
        private final String sql;

        private SearchType(String sql) {
            this.sql = sql;
        }

        public String getSql() {
            return sql;
        }
    }

    public List<UserModel> searchUsers(Connection dbConn, String q, SearchType searchType, int maxResults) throws Exception {
        String sql = searchType.getSql();
        PreparedStatement stmt = dbConn.prepareStatement(sql);
        stmt.setString(1, q);
        stmt.setInt(2, maxResults);

        ResultSet resultSet = stmt.executeQuery();
        List<UserModel> users = new ArrayList<>();
        while (resultSet.next()) {
            users.add(getUserFromResultSet(resultSet));
        }
        return users;
    }

    public UserModel insertNewUser(Connection dbConn, UserModel validatedUserInput)
            throws SQLException, URISyntaxException, WordDbException {

        // Check if a user with the same device ID already exists
        Optional<UserModel> userWithDevice = getUserByDeviceId(dbConn, validatedUserInput.getDeviceId());
        if (userWithDevice.isPresent()) {
            throw new ConflictException("deviceId", "A user has already been created for the given device");
        }

        // Check if a user with the same username already exists
        Optional<UserModel> userWithUsername = getUserByUsername(dbConn, validatedUserInput.getUsername(), false);
        if (userWithUsername.isPresent()) {
            throw new ConflictException("username", String.format("A user with the username '%s' already exists.", validatedUserInput.getUsername()));
        }
        // If an email is given, check if a user with the same email exists.
        if (validatedUserInput.getEmail() != null) {
            Optional<UserModel> userWithEmail = getUserByEmail(dbConn, validatedUserInput.getEmail());
            if (userWithEmail.isPresent()) {
                throw new ConflictException("email", String.format("A user with email '%s' already exists.", validatedUserInput.getEmail()));
            }
        }
        PreparedStatement stmt = dbConn.prepareStatement(INSERT_USER_QUERY, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, validatedUserInput.getUsername());
        // Set the email, which can be null
        if (Strings.isNullOrEmpty(validatedUserInput.getEmail())) {
            stmt.setNull(2, Types.VARCHAR);
        } else {
            stmt.setString(2, validatedUserInput.getEmail());
        }
        // Set the device ID, which can be null
        if (validatedUserInput.getDeviceId() == null) {
            stmt.setNull(3, Types.VARCHAR);
        } else {
            stmt.setString(3, validatedUserInput.getDeviceId());
        }

        // Set the current time
        stmt.setTimestamp(4, new Timestamp(new java.util.Date().getTime()));
        // Set whether it's a system user.
        boolean isSystemUser = validatedUserInput.getSystemUser() != null && validatedUserInput.getSystemUser();
        stmt.setBoolean(5, isSystemUser);
        stmt.setInt(6, EloRankingComputer.getInitialUserRating()); // Users start with default rating of 1500 + a random value in [0, 1)
        stmt.executeUpdate();

        // Populate the returned user from the result
        ResultSet result = stmt.getGeneratedKeys();
        result.next();

        return getUserFromResultSet(result);
    }

    public UserModel updateUserPassword(Connection dbConn, int userId, String hashPassBase64, String saltBase64) throws SQLException {
        if (Strings.isNullOrEmpty(hashPassBase64) || Strings.isNullOrEmpty(saltBase64)) {
            throw new IllegalArgumentException();
        }
        Optional<UserModel> user = getUserById(dbConn, userId);
        if (!user.isPresent()) {
            throw new IllegalArgumentException("No user exists with the given ID of " + userId);
        }
        PreparedStatement stmt = dbConn.prepareStatement(UPDATE_USER_PASSWORD, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, hashPassBase64);
        stmt.setString(2, saltBase64);
        stmt.setInt(3, userId);

        stmt.executeUpdate();
        ResultSet resultSet = stmt.getGeneratedKeys();
        resultSet.next();

        return getUserFromResultSet(resultSet);
    }

    public boolean isUserPasswordDefined(Connection dbConn, int userId) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(IS_USER_PASSWORD_DEFINED);
        stmt.setInt(1, userId);

        ResultSet resultSet = stmt.executeQuery();
        if (!resultSet.next()) {
            throw new SQLException("0 results returned checking if password is defined for user id = " + userId);
        }

        return resultSet.getBoolean("is_pass_defined");
    }

    public void updateUserRating(Connection dbConn, int userId, int newRating, UserRecordChange userRecordChange) throws SQLException {
        String sql;
        switch (userRecordChange) {
            case INCREASE_WINS:
                sql = UPDATE_USER_RATING_WIN;
                break;
            case INCREASE_LOSSES:
                sql = UPDATE_USER_RATING_LOSE;
                break;
            case INCREASE_TIES:
                sql = UPDATE_USER_RATING_TIE;
                break;
            default:
                LOG.error("Error - attempt to update user rating for userId {} and UserRecordChange = {}", userId, userRecordChange);
                return;
        }
        PreparedStatement stmt = dbConn.prepareStatement(sql);
        stmt.setInt(1, newRating);
        stmt.setInt(2, userId);

        int numUpdated = stmt.executeUpdate();
        if (numUpdated != 1) {
            throw new SQLException(
                    String.format("Error - expected 1 user's rating to be updated for userId = %d, but numUpdated = %d ",
                            userId, numUpdated));
        }
    }

    public Optional<UserModel> getUserById(Connection dbConn, int id) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(GET_USER_BY_ID_QUERY);
        stmt.setInt(1, id);
        ResultSet result = stmt.executeQuery();
        if (!result.next()) {
            return Optional.empty();
        }
        return Optional.of(getUserFromResultSet(result));
    }

    public Optional<UserModel> getUserByDeviceId(Connection dbConn, String deviceId) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(GET_USER_BY_DEVICE_ID_QUERY);
        stmt.setString(1, deviceId);
        ResultSet result = stmt.executeQuery();
        if (!result.next()) {
            return Optional.empty();
        }
        return Optional.of(getUserFromResultSet(result));
    }

    public Optional<UserModel> getUserByUsername(Connection dbConn, String username, boolean caseSensitive) throws SQLException {
        final String sql = caseSensitive ? GET_USER_BY_USERNAME_QUERY : GET_USER_CASE_INSENSITIVE;
        PreparedStatement stmt = dbConn.prepareStatement(sql);
        stmt.setString(1, username);
        ResultSet result = stmt.executeQuery();
        if (!result.next()) {
            return Optional.empty();
        }
        return Optional.of(getUserFromResultSet(result));
    }

    public Optional<UserModel> getUserByEmail(Connection dbConn, String email) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(GET_USER_BY_EMAIL_QUERY);
        stmt.setString(1, email);
        ResultSet result = stmt.executeQuery();
        if (!result.next()) {
            return Optional.empty();
        }
        return Optional.of(getUserFromResultSet(result));
    }

    public List<UserModel> getUsersWithRatingGEQ(Connection dbConn, final int userId, final int dbRating, final int limit) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(GET_USERS_WITH_RATING_GEQ);
        stmt.setInt(1, dbRating);
        stmt.setInt(2, userId);
        stmt.setInt(3, limit);

        ResultSet resultSet = stmt.executeQuery();
        List<UserModel> results = new ArrayList<>(limit);
        while (resultSet.next()) {
            results.add(getUserFromResultSet(resultSet));
        }
        return results;
    }

    public List<UserModel> getUsersWithRatingLT(Connection dbConn, final int dbRating, final int limit) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(GET_USERS_WITH_RATING_LT);
        stmt.setInt(1, dbRating);
        stmt.setInt(2, limit);

        ResultSet resultSet = stmt.executeQuery();
        List<UserModel> results = new ArrayList<>(limit);
        while (resultSet.next()) {
            results.add(0, getUserFromResultSet(resultSet));
        }
        return results;
    }

    // ------- Rank queries -----
    public Optional<UserModel> getUserWithRank(Connection dbConn, int userId) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(GET_USER_WITH_RANK);
        stmt.setInt(1, userId);
        ResultSet resultSet = stmt.executeQuery();
        if (!resultSet.next()) {
            return Optional.empty();
        }
        UserModel userModel = getUserFromResultSetWithRank(resultSet);
        return Optional.of(userModel);
    }

    public List<UserModel> getUsersWithRankLT(Connection dbConn, final int rating, final int limit) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(GET_USERS_WITH_RANK_LT_BY_RATING);
        stmt.setInt(1, rating);
        stmt.setInt(2, limit);

        ResultSet resultSet = stmt.executeQuery();
        List<UserModel> results = new ArrayList<>(limit);
        while (resultSet.next()) {
            results.add(getUserFromResultSetWithRank(resultSet));
        }
        Collections.reverse(results);
        return results;
    }

    public List<UserModel> getUsersWithRankGT(Connection dbConn, final int rank, final int limit) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(GET_USERS_WITH_RANK_GT_BY_RATING);
        stmt.setInt(1, rank);
        stmt.setInt(2, limit);

        ResultSet resultSet = stmt.executeQuery();
        List<UserModel> results = new ArrayList<>(limit);
        while (resultSet.next()) {
            results.add(getUserFromResultSetWithRank(resultSet));
        }
        return results;
    }

    public List<UserModel> getUsersWithBestRanks(Connection dbConn, final int limit) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(GET_BEST_RANKED_USERS);
        stmt.setInt(1, limit);

        ResultSet resultSet = stmt.executeQuery();
        List<UserModel> results = new ArrayList<>(limit);
        while (resultSet.next()) {
            results.add(getUserFromResultSetWithRank(resultSet));
        }
        return results;
    }

    public int getNumGamesMyTurn(Connection dbConn, int userId) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(GET_NUM_GAMES_MY_TURN);
        stmt.setInt(1, userId);
        stmt.setInt(2, userId);
        stmt.setShort(3, (short) GameResult.IN_PROGRESS.ordinal());
        stmt.setInt(4, userId);
        stmt.setShort(5, (short) GameResult.OFFERED.ordinal());
        ResultSet resultSet = stmt.executeQuery();

        if (!resultSet.next()) {
            throw new SQLException("Query to get number of games where it's the current user's turn failed and returned 0 results!");
        }

        return resultSet.getInt("count");
    }

    public int getNumGamesOfferedToMe(Connection dbConn, int userId) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(GET_NUM_GAMES_OFFERED_TO_ME);
        stmt.setInt(1, userId);
        stmt.setShort(2, (short) GameResult.OFFERED.ordinal());
        ResultSet resultSet = stmt.executeQuery();

        if (!resultSet.next()) {
            throw new SQLException("Query to get number of games offered to current user failed and returned 0 results!");
        }

        return resultSet.getInt("count");
    }

    public void incrementAllUserTokens() throws SQLException {
        final int BATCH_SIZE = 1000;
        LOG.info("[START TASK] Starting to increment tokens for all users in batches of 1,000 users...");
        final long START = System.currentTimeMillis();
        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            final int MAX_ID = getMaximumId(dbConn);
            int lowerId = 0;
            while (lowerId <= MAX_ID) {
                int upperId = lowerId + BATCH_SIZE;
                PreparedStatement stmt = dbConn.prepareStatement(INCREMENT_TOKENS);
                stmt.setInt(1, lowerId);
                stmt.setInt(2, upperId);
                int numAffected = stmt.executeUpdate();
                LOG.info("Incremented tokens for user ids {} - {}, users updated: {}.", lowerId, upperId, numAffected);
                lowerId += BATCH_SIZE;
            }
        }
        final long END = System.currentTimeMillis();
        LOG.warn("[END TASK] Duration to increment all users' tokens by 1: {}s", TimeUnit.MILLISECONDS.toSeconds(END - START));
    }

    public UserModel increaseUsersTokensForPurchase(int userId, int numTokens) throws SQLException {
        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            PreparedStatement stmt = dbConn.prepareStatement(INCREASE_TOKENS_FROM_PURCHASE, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, numTokens);
            stmt.setInt(2, userId);
            int num = stmt.executeUpdate();
            if (num != 1) {
                throw new SQLException("Expected 1 row to be updated, instead was: " + num);
            }
            ResultSet resultSet = stmt.getGeneratedKeys();
            resultSet.next();
            return getUserFromResultSet(resultSet);
        }
    }

    public UserModel spendTokens(Connection dbConn, int userId, int numTokens) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(SPEND_TOKENS, Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, numTokens);
        stmt.setInt(2, userId);
        int num = stmt.executeUpdate();
        if (num != 1) {
            throw new SQLException("Expected 1 row to be updated, instead was: " + num);
        }
        ResultSet resultSet = stmt.getGeneratedKeys();
        resultSet.next();
        return getUserFromResultSet(resultSet);
    }

    public UserModel grantInfiniteBooks(int userId) throws SQLException {
        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            PreparedStatement stmt = dbConn.prepareStatement(GRANT_INFINITE_BOOKS, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, userId);
            int num = stmt.executeUpdate();
            if (num != 1) {
                throw new SQLException("Expected 1 row to be updated when granting infinite books, instead was: " + num);
            }
            ResultSet resultSet = stmt.getGeneratedKeys();
            resultSet.next();
            return getUserFromResultSet(resultSet);
        }
    }

    public int getMaximumId(Connection dbConn) throws SQLException {
        Statement stmt = dbConn.createStatement();
        ResultSet resultSet = stmt.executeQuery("SELECT MAX(id) AS max_id FROM word_users;");
        if (!resultSet.next()) {
            throw new SQLException("No result found for the max ID");
        }
        return resultSet.getInt("max_id");
    }

    // ------------ Private ---------
    private UserModel getUserFromResultSet(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String username = resultSet.getString("username");
        String email = resultSet.getString("email");
        String hashpass = resultSet.getString("hashpass");
        String salt = resultSet.getString("salt");
        Timestamp dateJoined = resultSet.getTimestamp("date_joined");
        boolean systemUser = resultSet.getBoolean("is_system_user");
        int rating = resultSet.getInt("rating");
        int wins = resultSet.getInt("wins");
        int losses = resultSet.getInt("losses");
        int ties = resultSet.getInt("ties");
        int tokens = resultSet.getInt("tokens");
        boolean infiniteBooks = resultSet.getBoolean("infinite_books");
        UserModel user = new UserModel(id, username, email, null, new UserHashInfo(hashpass, salt), systemUser);
        user.setDateJoined(dateJoined.getTime());
        user.setRating(rating);
        user.setWins(wins);
        user.setLosses(losses);
        user.setTies(ties);
        user.setTokens(tokens);
        user.setInfiniteBooks(infiniteBooks);
        return user;
    }

    private UserModel getUserFromResultSetWithRank(ResultSet resultSet) throws SQLException {
        UserModel userModel = getUserFromResultSet(resultSet);
        int rank = resultSet.getInt("word_rank");
        userModel.setRank(rank);
        return userModel;
    }
}
