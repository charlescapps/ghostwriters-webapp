package net.capps.word.db.dao;

import net.capps.word.db.WordDbManager;
import net.capps.word.game.common.Dir;
import net.capps.word.game.move.MoveType;
import net.capps.word.rest.models.MoveModel;
import net.capps.word.rest.models.PosModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by charlescapps on 1/22/15.
 */
public class MovesDAO {
    private static final MovesDAO INSTANCE = new MovesDAO();
    private static final Logger LOG = LoggerFactory.getLogger(MovesDAO.class);

    public static MovesDAO getInstance() {
        return INSTANCE;
    }

    private static final String INSERT_MOVE =
            "INSERT INTO word_moves (game_id, player_id, move_type, start_row, start_col, direction, word, tiles_played, points, date_played) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

    private static final String GET_RECENT_MOVES =
            "SELECT * FROM word_moves WHERE game_id = ? ORDER BY id DESC LIMIT ?";

    private static final String GET_MOST_RECENT_MOVE =
            "SELECT * FROM word_moves WHERE game_id = ? ORDER BY id DESC LIMIT 1";

    private static final String DELETE_OLD_MOVES =
            "DELETE FROM word_moves WHERE EXTRACT(EPOCH FROM NOW()) - EXTRACT(EPOCH FROM date_played) > ?";


    public List<MoveModel> getMostRecentMoves(int gameId, int limit, Connection dbConn) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(GET_RECENT_MOVES);
        stmt.setInt(1, gameId);
        stmt.setInt(2, limit);
        ResultSet resultSet = stmt.executeQuery();
        List<MoveModel> moves = new ArrayList<>(limit);
        while (resultSet.next()) {
            moves.add(getMoveFromResult(resultSet));
        }
        return moves;
    }

    public Optional<MoveModel> getMostRecentMove(int gameId, Connection dbConn) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(GET_MOST_RECENT_MOVE);
        stmt.setInt(1, gameId);
        ResultSet resultSet = stmt.executeQuery();
        if (resultSet.next()) {
            return Optional.of(getMoveFromResult(resultSet));
        }
        return Optional.empty();
    }

    public MoveModel insertMove(MoveModel inputMove, int numPoints, Connection dbConn) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(INSERT_MOVE, Statement.RETURN_GENERATED_KEYS);
        inputMove.setPoints(numPoints);
        stmt.setInt(1, inputMove.getGameId());
        stmt.setInt(2, inputMove.getPlayerId());
        stmt.setShort(3, (short) inputMove.getMoveType().ordinal());
        stmt.setShort(4, (short) inputMove.getStart().getR());
        stmt.setShort(5, (short) inputMove.getStart().getC());
        stmt.setString(6, inputMove.getDir().toString());
        stmt.setString(7, inputMove.getLetters());
        stmt.setString(8, inputMove.getTiles());
        stmt.setInt(9, numPoints);
        stmt.setTimestamp(10, new Timestamp(new Date().getTime()));

        int rowCount = stmt.executeUpdate();
        if (rowCount != 1) {
            throw new SQLException("Row count wasn't 1 after inserting a move. Row count was: " + rowCount);
        }

        ResultSet result = stmt.getGeneratedKeys();
        if (!result.next()) {
            throw new SQLException("There was no result returned after inserting a move!");
        }

        return getMoveFromResult(result);
    }

    public List<MoveModel> getPreviousMoveByPlayer(int playerId, int gameId, Connection dbConn) throws SQLException {
        Optional<MoveModel> recentMoveOpt = getMostRecentMove(gameId, dbConn);
        List<MoveModel> recentMoveSingleton = new ArrayList<>(1);
        if (recentMoveOpt.isPresent()) {
            MoveModel recentMove = recentMoveOpt.get();
            if (recentMove.getPlayerId() == playerId) { // Integer will unbox
                recentMoveSingleton.add(recentMove);
            }
        }

        return recentMoveSingleton;
    }

    public void deleteOldMoves(final int numberOfSecondsForExpiration) throws SQLException {
        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            PreparedStatement stmt = dbConn.prepareStatement(DELETE_OLD_MOVES);
            stmt.setInt(1, numberOfSecondsForExpiration);
            int numAffected = stmt.executeUpdate();
            LOG.info("====Deleted {} old moves from the word_moves table.", numAffected);
        }
    }

    // ------------- Private --------------
    private MoveModel getMoveFromResult(ResultSet result) throws SQLException {
        return new MoveModel(result.getInt("game_id"),
                result.getInt("player_id"),
                MoveType.values()[result.getShort("move_type")],
                result.getString("word"),
                new PosModel(result.getShort("start_row"), result.getShort("start_col")),
                Dir.valueOf(result.getString("direction")),
                result.getString("tiles_played"),
                result.getInt("points"),
                result.getTimestamp("date_played").getTime());
    }


}
