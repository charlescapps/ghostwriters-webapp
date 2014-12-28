package net.capps.word.db.dao;

import com.google.common.base.Optional;
import net.capps.word.db.WordDbManager;
import net.capps.word.models.WordUserModel;

import java.net.URISyntaxException;
import java.sql.*;

/**
 * Created by charlescapps on 12/26/14.
 */
public class WordUsersDAO {
    private static final String INSERT_USER_QUERY =
            "INSERT INTO word_users (username, email, hashpass, salt) " +
            "VALUES (?, ?, ?, ?);";

    private static final String GET_USER_QUERY =
            "SELECT * FROM word_users WHERE id = ?;";

    public WordUserModel insertNewUser(WordUserModel validatedUserInput, byte[] hashPass, byte[] salt)
            throws SQLException, URISyntaxException {
        try(Connection dbConn = WordDbManager.getInstance().getConnection()) {
            PreparedStatement stmt = dbConn.prepareStatement(INSERT_USER_QUERY, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, validatedUserInput.getUsername());
            stmt.setString(2, validatedUserInput.getEmail());
            stmt.setBytes(3, hashPass);
            stmt.setBytes(4, salt);
            stmt.executeUpdate();

            // Populate the returned user from the result
            ResultSet result = stmt.getGeneratedKeys();
            result.next(); // This should always succeed since no SQL exception was thrown!
            int id = result.getInt("id");
            String username = result.getString("username");
            String email = result.getString("email");

            return new WordUserModel(id, username, email, null);
        }
    }

    public Optional<WordUserModel> getUserById(int id) throws SQLException, URISyntaxException {
        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            PreparedStatement stmt = dbConn.prepareStatement(GET_USER_QUERY);
            stmt.setInt(1, id);
            ResultSet result = stmt.executeQuery();
            if (!result.next()) {
                return Optional.absent();
            }
            String username = result.getString("username");
            String email = result.getString("email");
            return Optional.of(new WordUserModel(id, username, email, null));
        }
    }
}
