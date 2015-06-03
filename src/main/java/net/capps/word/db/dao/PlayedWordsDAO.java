package net.capps.word.db.dao;

import net.capps.word.game.dict.SpecialDict;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Created by charlescapps on 6/2/15.
 */
public class PlayedWordsDAO {

    private static final PlayedWordsDAO INSTANCE = new PlayedWordsDAO();

    // Queries
    private static final String GET_WORD_MAP =
            "SELECT encode(word_map, 'hex') AS hex_map FROM played_words WHERE user_id = ? AND special_dict = ?;";

    private static final String INSERT_WORD_MAP =
            "INSERT INTO played_words (user_id, special_dict, word_map) " +
            "VALUES (?, ?, decode(?, 'hex'))";

    private static final String UPDATE_WORD_MAP =
            "UPDATE played_words SET word_map = decode(?, 'hex') WHERE user_id = ? AND special_dict = ?;";

    public static PlayedWordsDAO getInstance() {
        return INSTANCE;
    }

    public Optional<String> getWordMap(Connection dbConn, int userId, SpecialDict specialDict) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(GET_WORD_MAP);
        stmt.setInt(1, userId);
        stmt.setShort(2, (short)(specialDict.ordinal()));

        ResultSet resultSet = stmt.executeQuery();
        if (!resultSet.next()) {
            return Optional.empty();
        }

        String wordMap = resultSet.getString("hex_map");
        return Optional.of(wordMap);
    }

    public void insertWordMap(Connection dbConn, int userId, SpecialDict specialDict, String hexString) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(INSERT_WORD_MAP);
        if (hexString.length() % 2 == 1) {
            hexString += '0';  // Postgres requires an even number of digits.
        }
        stmt.setInt(1, userId);
        stmt.setShort(2, (short)(specialDict.ordinal()));
        stmt.setString(3, hexString);

        int updated = stmt.executeUpdate();
        if (updated != 1) {
            throw new SQLException("Expected 1 row to be updated when inserting new word map, but result was: " + updated);
        }
    }

    public void updateWordMap(Connection dbConn, int userId, SpecialDict specialDict, String hexString) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(UPDATE_WORD_MAP);
        if (hexString.length() % 2 == 1) {
            hexString += '0';  // Postgres requires an even number of digits.
        }
        stmt.setString(1, hexString);
        stmt.setInt(2, userId);
        stmt.setShort(3, (short) (specialDict.ordinal()));

        int updated = stmt.executeUpdate();
        if (updated != 1) {
            throw new SQLException("Expected 1 row to be updated when inserting new word map, but result was: " + updated);
        }
    }
}
