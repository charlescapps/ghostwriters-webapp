package net.capps.word.db.dao;

import com.google.common.base.Optional;
import net.capps.word.db.WordDbManager;
import net.capps.word.rest.models.SessionModel;

import java.sql.*;
import java.util.Date;

/**
 * Created by charlescapps on 2/5/15.
 */
public class SessionsDAO {
    private static final SessionsDAO INSTANCE = new SessionsDAO();
    private static final WordDbManager WORD_DB_MANAGER = WordDbManager.getInstance();

    private static final String INSERT_SESSION_QUERY =
            "INSERT INTO word_sessions (user_id, session_id, date_created) VALUES (?, ?, ?);";

    private static final String GET_SESSION_FOR_USER =
            "SELECT * FROM word_sessions WHERE user_id = ?;";

    private static final String GET_SESSION_FOR_SESSION_ID =
            "SELECT * FROM word_sessions WHERE session_id = ?;";

    private static final String DELETE_SESSION_FOR_USER =
            "DELETE FROM word_sessions WHERE user_id = ?;";

    public static SessionsDAO getInstance() {
        return INSTANCE;
    }

    private SessionsDAO() { } // Singleton pattern

    public SessionModel insertSession(int userId, String sessionId) throws SQLException {
        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            PreparedStatement stmt = dbConn.prepareStatement(INSERT_SESSION_QUERY, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, userId);
            stmt.setString(2, sessionId);
            stmt.setTimestamp(3, new Timestamp(new Date().getTime()));
            int numRows = stmt.executeUpdate();
            if (numRows != 1) {
                throw new SQLException("Failed to insert new session, expected 1 row affected, but had: " + numRows);
            }
            ResultSet resultSet = stmt.getGeneratedKeys();
            if (!resultSet.next()) {
                throw new SQLException("Failed to insert a new session, no results were returned.");
            }
            return createSessionFromCurrentRow(resultSet);
        }
    }

    public Optional<SessionModel> getSessionForUserId(int userId) throws SQLException {
        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            PreparedStatement stmt = dbConn.prepareStatement(GET_SESSION_FOR_USER);
            stmt.setInt(1, userId);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return Optional.of(createSessionFromCurrentRow(resultSet));
            }
            return Optional.absent();
        }
    }

    public Optional<SessionModel> getSessionForSessionId(String sessionId) throws SQLException {
        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            PreparedStatement stmt = dbConn.prepareStatement(GET_SESSION_FOR_SESSION_ID);
            stmt.setString(1, sessionId);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return Optional.of(createSessionFromCurrentRow(resultSet));
            }
            return Optional.absent();
        }
    }

    public int deleteSessionForUser(int userId) throws SQLException {
        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            PreparedStatement stmt = dbConn.prepareStatement(DELETE_SESSION_FOR_USER);
            stmt.setInt(1, userId);
            return stmt.executeUpdate();
        }
    }

    private SessionModel createSessionFromCurrentRow(ResultSet resultSet) throws SQLException {
        return new SessionModel(resultSet.getLong("id"),
                                resultSet.getString("session_id"),
                                resultSet.getInt("user_id"),
                                resultSet.getTimestamp("date_created").getTime());
    }
}
