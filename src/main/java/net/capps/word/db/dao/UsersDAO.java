package net.capps.word.db.dao;

import com.google.common.base.Optional;
import net.capps.word.db.WordDbManager;
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

    private static final String GET_USER_QUERY =
            "SELECT * FROM word_users WHERE id = ?;";

    public UserModel insertNewUser(UserModel validatedUserInput, String hashPassBase64, String saltBase64)
            throws SQLException, URISyntaxException {
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

            return new UserModel(id, username, email, null);
        }
    }

    public Optional<UserModel> getUserById(int id) throws SQLException, URISyntaxException {
        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            PreparedStatement stmt = dbConn.prepareStatement(GET_USER_QUERY);
            stmt.setInt(1, id);
            ResultSet result = stmt.executeQuery();
            if (!result.next()) {
                return Optional.absent();
            }
            String username = result.getString("username");
            String email = result.getString("email");
            return Optional.of(new UserModel(id, username, email, null));
        }
    }
}
