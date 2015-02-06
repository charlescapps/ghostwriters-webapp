package net.capps.word.db.dao;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.capps.word.db.WordDbManager;
import net.capps.word.exceptions.ConflictException;
import net.capps.word.exceptions.WordDbException;
import net.capps.word.rest.models.UserModel;

import java.net.URISyntaxException;
import java.sql.*;
import java.util.List;

/**
 * Created by charlescapps on 12/26/14.
 */
public class UsersDAO {
    private static final String INSERT_USER_QUERY =
            "INSERT INTO word_users (username, email, hashpass, salt, date_joined) " +
            "VALUES (?, ?, ?, ?, ?);";

    private static final String GET_USER_BY_ID_QUERY =
            "SELECT * FROM word_users WHERE id = ?;";

    private static final String GET_USER_BY_USERNAME_QUERY =
            "SELECT * FROM word_users WHERE username = ?;";

    private static final String GET_USER_BY_EMAIL_QUERY =
            "SELECT * FROM word_users WHERE email = ?;";

    private static final String GET_USER_CASE_INSENSITIVE =
            "SELECT * FROM word_users WHERE lower(username) = lower(?);";

    private static final String PREFIX_SEARCH_QUERY =
            "SELECT * FROM word_users WHERE lower(username) LIKE (lower(?) || '%') ORDER BY username ASC LIMIT ?";

    private static final String SUBSTRING_SEARCH_QUERY =
            "SELECT * FROM word_users WHERE strpos(lower(username), lower(?)) > 0 ORDER BY username ASC LIMIT ?";

    private static final UsersDAO INSTANCE = new UsersDAO();

    public static final UsersDAO getInstance() {
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
        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            String sql = searchType.getSql();
            PreparedStatement stmt = dbConn.prepareStatement(sql);
            stmt.setString(1, q);
            stmt.setInt(2, maxResults);

            ResultSet resultSet = stmt.executeQuery();
            List<UserModel> users = Lists.newArrayList();
            while (resultSet.next()) {
                users.add(getUserFromResultSet(resultSet));
            }
            return users;
        }
    }

    public UserModel insertNewUser(UserModel validatedUserInput, String hashPassBase64, String saltBase64)
            throws SQLException, URISyntaxException, WordDbException {
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
        try(Connection dbConn = WordDbManager.getInstance().getConnection()) {
            PreparedStatement stmt = dbConn.prepareStatement(INSERT_USER_QUERY, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, validatedUserInput.getUsername());
            stmt.setString(2, validatedUserInput.getEmail());
            stmt.setString(3, hashPassBase64);
            stmt.setString(4, saltBase64);
            stmt.setTimestamp(5, new Timestamp(new java.util.Date().getTime()));
            stmt.executeUpdate();

            // Populate the returned user from the result
            ResultSet result = stmt.getGeneratedKeys();
            result.next();

            return getUserFromResultSet(result);
        }
    }

    public Optional<UserModel> getUserById(int id) throws SQLException {
        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            PreparedStatement stmt = dbConn.prepareStatement(GET_USER_BY_ID_QUERY);
            stmt.setInt(1, id);
            ResultSet result = stmt.executeQuery();
            if (!result.next()) {
                return Optional.absent();
            }
            return Optional.of(getUserFromResultSet(result));
        }
    }

    public Optional<UserModel> getUserByUsername(String username, boolean caseSensitive) throws SQLException {
        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
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
        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            PreparedStatement stmt = dbConn.prepareStatement(GET_USER_BY_EMAIL_QUERY);
            stmt.setString(1, email);
            ResultSet result = stmt.executeQuery();
            if (!result.next()) {
                return Optional.absent();
            }
            return Optional.of(getUserFromResultSet(result));
        }
    }

    private UserModel getUserFromResultSet(ResultSet result) throws SQLException {
        int id = result.getInt("id");
        String username = result.getString("username");
        String email = result.getString("email");
        String hashpass = result.getString("hashpass");
        String salt = result.getString("salt");
        Timestamp dateJoined = result.getTimestamp("date_joined");
        UserModel user = new UserModel(id, username, email, null, new UserHashInfo(hashpass, salt));
        user.setDateJoined(dateJoined.getTime());
        return user;
    }
}
