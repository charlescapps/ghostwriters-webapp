package net.capps.word.db.dao;

import com.google.common.base.Optional;
import net.capps.word.db.WordDbManager;
import net.capps.word.exceptions.ConflictException;
import net.capps.word.exceptions.WordDbException;
import net.capps.word.models.UserModel;

import java.net.URISyntaxException;
import java.sql.*;

/**
 * Created by charlescapps on 12/26/14.
 */
public class UsersDAO {
    private static final String INSERT_USER_QUERY =
            "INSERT INTO word_users (username, email, hashpass, salt) " +
            "VALUES (?, ?, ?, ?);";

    private static final String GET_USER_BY_ID_QUERY =
            "SELECT * FROM word_users WHERE id = ?;";

    private static final String GET_USER_BY_USERNAME_QUERY =
            "SELECT * FROM word_users WHERE username = ?;";

    private static final String GET_USER_BY_EMAIL_QUERY =
            "SELECT * FROM word_users WHERE email = ?;";

    private static final UsersDAO INSTANCE = new UsersDAO();

    public static final UsersDAO getInstance() {
        return INSTANCE;
    }

    public UserModel insertNewUser(UserModel validatedUserInput, String hashPassBase64, String saltBase64)
            throws SQLException, URISyntaxException, WordDbException {
        // Check if a user with the same username already exists
        Optional<UserModel> userWithUsername = getUserByUsername(validatedUserInput.getUsername());
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
            stmt.executeUpdate();

            // Populate the returned user from the result
            ResultSet result = stmt.getGeneratedKeys();
            result.next(); // This should always succeed since no SQL exception was thrown!
            int id = result.getInt("id");
            String username = result.getString("username");
            String email = result.getString("email");

            return new UserModel(id, username, email, null, null);
        }
    }

    public Optional<UserModel> getUserById(int id) throws SQLException, URISyntaxException {
        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            PreparedStatement stmt = dbConn.prepareStatement(GET_USER_BY_ID_QUERY);
            stmt.setInt(1, id);
            ResultSet result = stmt.executeQuery();
            if (!result.next()) {
                return Optional.absent();
            }
            String username = result.getString("username");
            String email = result.getString("email");
            return Optional.of(new UserModel(id, username, email, null, null));
        }
    }

    public Optional<UserModel> getUserByUsername(String username) throws SQLException, URISyntaxException {
        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            PreparedStatement stmt = dbConn.prepareStatement(GET_USER_BY_USERNAME_QUERY);
            stmt.setString(1, username);
            ResultSet result = stmt.executeQuery();
            if (!result.next()) {
                return Optional.absent();
            }
            int id = result.getInt("id");
            String email = result.getString("email");
            String hashpass = result.getString("hashpass");
            String salt = result.getString("salt");
            return Optional.of(new UserModel(id, username, email, null, new UserHashInfo(hashpass, salt)));
        }
    }

    public Optional<UserModel> getUserByEmail(String email) throws SQLException, URISyntaxException {
        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            PreparedStatement stmt = dbConn.prepareStatement(GET_USER_BY_EMAIL_QUERY);
            stmt.setString(1, email);
            ResultSet result = stmt.executeQuery();
            if (!result.next()) {
                return Optional.absent();
            }
            int id = result.getInt("id");
            String username = result.getString("username");
            String hashpass = result.getString("hashpass");
            String salt = result.getString("salt");
            return Optional.of(new UserModel(id, username, email, null, new UserHashInfo(hashpass, salt)));
        }
    }
}
