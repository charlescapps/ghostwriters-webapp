package net.capps.word.db.dao;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import net.capps.word.constants.WordConstants;
import net.capps.word.db.WordDbManager;
import net.capps.word.exceptions.ConflictException;
import net.capps.word.exceptions.WordDbException;
import net.capps.word.game.ranking.EloRankingComputer;
import net.capps.word.rest.models.UserModel;
import net.capps.word.rest.providers.UserRecordChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by charlescapps on 12/26/14.
 */
public class UsersDAO {
    private static final Logger LOG = LoggerFactory.getLogger(UsersDAO.class);
    private static final WordDbManager WORD_DB_MANAGER = WordDbManager.getInstance();
    
    private static final String INSERT_USER_QUERY =
            "INSERT INTO word_users (username, email, device_id, date_joined, is_system_user, rating) " +
            "VALUES (?, ?, ?, ?, ?, ?);";

    private static final String UPDATE_USER_PASSWORD =
            "UPDATE word_users SET (hashpass, salt) = (?, ?) WHERE id = ?";

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
            "SELECT * FROM word_users WHERE lower(username) LIKE (lower(?) || '%') AND id != ? ORDER BY username ASC LIMIT ?";

    private static final String SUBSTRING_SEARCH_QUERY =
            "SELECT * FROM word_users WHERE strpos(lower(username), lower(?)) > 0 AND id != ? ORDER BY username ASC LIMIT ?";

    // Get users by ratings
    private static final String GET_USERS_WITH_RATING_GEQ =
            "SELECT * FROM word_users WHERE rating >= ? AND id != ? AND id != ? ORDER BY rating DESC LIMIT ?";

    private static final String GET_USERS_WITH_RATING_LT =
            "SELECT * FROM word_users WHERE rating < ? AND id != ? ORDER BY rating DESC LIMIT ?";

    // User ranking based on rating
    public static final String CREATE_RANKING_VIEW =
            "CREATE OR REPLACE VIEW word_user_ranks AS " +
                "SELECT t1.*, COUNT(t2.id) AS rank FROM word_users t1 INNER JOIN word_users t2 ON t1.rating >= t2.rating " +
                "ORDER BY rank DESC;";

    // Get users with ranking around a given user
    private static final String GET_USER_WITH_RANK =
            "SELECT * FROM word_user_ranks WHERE id = ?;";

    private static final String GET_USERS_WITH_RANK_LT =
            "SELECT * FROM word_user_ranks WHERE rating > ? AND id != ? ORDER BY rank DESC LIMIT ?";

    private static final String GET_USERS_WITH_RANK_GEQ =
            "SELECT * FROM word_user_ranks WHERE rating <= ? AND id != ? AND id != ? ORDER BY rank DESC LIMIT ?";

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

    public List<UserModel> searchUsers(String q, SearchType searchType, int maxResults) throws Exception {
        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            String sql = searchType.getSql();
            PreparedStatement stmt = dbConn.prepareStatement(sql);
            stmt.setString(1, q);
            stmt.setInt(2, WordConstants.INITIAL_USER.get().getId());
            stmt.setInt(3, maxResults);

            ResultSet resultSet = stmt.executeQuery();
            List<UserModel> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(getUserFromResultSet(resultSet));
            }
            return users;
        }
    }

    public UserModel insertNewUser(UserModel validatedUserInput)
            throws SQLException, URISyntaxException, WordDbException {

        // Check if a user with the same device ID already exists
        Optional<UserModel> userWithDevice = getUserByDeviceId(validatedUserInput.getDeviceId());
        if (userWithDevice.isPresent()) {
            throw new ConflictException("deviceId", "A user has already been created for the given device");
        }

        // Check if a user with the same username already exists
        Optional<UserModel> userWithUsername = getUserByUsername(validatedUserInput.getUsername(), false);
        if (userWithUsername.isPresent()) {
            throw new ConflictException("username", String.format("A user with the username '%s' already exists.", validatedUserInput.getUsername()));
        }
        // If an email is given, check if a user with the same email exists.
        if (validatedUserInput.getEmail() != null) {
            Optional<UserModel> userWithEmail = getUserByEmail(validatedUserInput.getEmail());
            if (userWithEmail.isPresent()) {
                throw new ConflictException("email", String.format("A user with email '%s' already exists.", validatedUserInput.getEmail()));
            }
        }
        try(Connection dbConn = WORD_DB_MANAGER.getConnection()) {
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
            stmt.setInt(6, EloRankingComputer.AVERAGE_RATING_DB); // Users start with default rating of 1500
            stmt.executeUpdate();

            // Populate the returned user from the result
            ResultSet result = stmt.getGeneratedKeys();
            result.next();

            return getUserFromResultSet(result);
        }
    }

    public UserModel updateUserPassword(int userId, String hashPassBase64, String saltBase64) throws SQLException {
        if (Strings.isNullOrEmpty(hashPassBase64) || Strings.isNullOrEmpty(saltBase64)) {
            throw new IllegalArgumentException();
        }
        Optional<UserModel> user = getUserById(userId);
        if (!user.isPresent()) {
            throw new IllegalArgumentException("No user exists with the given ID of " + userId);
        }
        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            PreparedStatement stmt = dbConn.prepareStatement(UPDATE_USER_PASSWORD, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, hashPassBase64);
            stmt.setString(2, saltBase64);
            stmt.setInt(3, userId);

            stmt.executeUpdate();
            ResultSet resultSet = stmt.getGeneratedKeys();
            resultSet.next();

            return getUserFromResultSet(resultSet);
        }
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

    public Optional<UserModel> getUserById(int id) throws SQLException {
        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            PreparedStatement stmt = dbConn.prepareStatement(GET_USER_BY_ID_QUERY);
            stmt.setInt(1, id);
            ResultSet result = stmt.executeQuery();
            if (!result.next()) {
                return Optional.absent();
            }
            return Optional.of(getUserFromResultSet(result));
        }
    }

    public Optional<UserModel> getUserByDeviceId(String deviceId) throws SQLException {
        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            PreparedStatement stmt = dbConn.prepareStatement(GET_USER_BY_DEVICE_ID_QUERY);
            stmt.setString(1, deviceId);
            ResultSet result = stmt.executeQuery();
            if (!result.next()) {
                return Optional.absent();
            }
            return Optional.of(getUserFromResultSet(result));
        }
    }

    public Optional<UserModel> getUserByUsername(String username, boolean caseSensitive) throws SQLException {
        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            final String sql = caseSensitive ? GET_USER_BY_USERNAME_QUERY : GET_USER_CASE_INSENSITIVE;
            PreparedStatement stmt = dbConn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet result = stmt.executeQuery();
            if (!result.next()) {
                return Optional.absent();
            }
            return Optional.of(getUserFromResultSet(result));
        }
    }

    public Optional<UserModel> getUserByEmail(String email) throws SQLException {
        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            PreparedStatement stmt = dbConn.prepareStatement(GET_USER_BY_EMAIL_QUERY);
            stmt.setString(1, email);
            ResultSet result = stmt.executeQuery();
            if (!result.next()) {
                return Optional.absent();
            }
            return Optional.of(getUserFromResultSet(result));
        }
    }
    
    public List<UserModel> getUsersWithRatingGEQ(final int userId, final int dbRating, final int limit) throws SQLException {
        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            PreparedStatement stmt = dbConn.prepareStatement(GET_USERS_WITH_RATING_GEQ);
            stmt.setInt(1, dbRating);
            stmt.setInt(2, userId);
            stmt.setInt(3, WordConstants.INITIAL_USER.get().getId());
            stmt.setInt(4, limit);

            ResultSet resultSet = stmt.executeQuery();
            List<UserModel> results = new ArrayList<>(limit);
            while (resultSet.next()) {
                results.add(getUserFromResultSet(resultSet));
            }
            return results;
        }
    }

    public List<UserModel> getUsersWithRatingLT(final int dbRating, final int limit) throws SQLException {
        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            PreparedStatement stmt = dbConn.prepareStatement(GET_USERS_WITH_RATING_LT);
            stmt.setInt(1, dbRating);
            stmt.setInt(2, WordConstants.INITIAL_USER.get().getId());
            stmt.setInt(3, limit);

            ResultSet resultSet = stmt.executeQuery();
            List<UserModel> results = new ArrayList<>(limit);
            while (resultSet.next()) {
                results.add(getUserFromResultSet(resultSet));
            }
            return results;
        }
    }

    // ------- Rank queries -----
    public Optional<UserModel> getUserWithRank(int userId) throws SQLException {
        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            PreparedStatement stmt = dbConn.prepareStatement(GET_USER_WITH_RANK);
            stmt.setInt(1, userId);
            ResultSet resultSet = stmt.executeQuery();
            if (!resultSet.next()) {
                return Optional.absent();
            }
            UserModel userModel = getUserFromResultSet(resultSet);
            int rank = resultSet.getInt("rank");
            userModel.setRank(rank);
            return Optional.of(userModel);
        }
    }

    public List<UserModel> getUsersWithRankLT(final int rating, final int limit) throws SQLException {
        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            PreparedStatement stmt = dbConn.prepareStatement(GET_USERS_WITH_RANK_LT);
            stmt.setInt(1, rating);
            stmt.setInt(2, WordConstants.INITIAL_USER.get().getId());
            stmt.setInt(3, limit);

            ResultSet resultSet = stmt.executeQuery();
            List<UserModel> results = new ArrayList<>(limit);
            while (resultSet.next()) {
                results.add(getUserFromResultSet(resultSet));
            }
            return results;
        }
    }

    public List<UserModel> getUsersWithRankGEQ(final int userId, final int rating, final int limit) throws SQLException {
        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            PreparedStatement stmt = dbConn.prepareStatement(GET_USERS_WITH_RANK_GEQ);
            stmt.setInt(1, rating);
            stmt.setInt(2, userId);
            stmt.setInt(3, WordConstants.INITIAL_USER.get().getId());
            stmt.setInt(4, limit);

            ResultSet resultSet = stmt.executeQuery();
            List<UserModel> results = new ArrayList<>(limit);
            while (resultSet.next()) {
                results.add(getUserFromResultSet(resultSet));
            }
            return results;
        }
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
        UserModel user = new UserModel(id, username, email, null, new UserHashInfo(hashpass, salt), systemUser);
        user.setDateJoined(dateJoined.getTime());
        user.setRating(rating);
        user.setWins(wins);
        user.setLosses(losses);
        user.setTies(ties);
        return user;
    }
}
