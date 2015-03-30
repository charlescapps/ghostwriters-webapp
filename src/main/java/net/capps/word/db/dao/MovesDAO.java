package net.capps.word.db.dao;

import net.capps.word.db.WordDbManager;
import net.capps.word.game.common.Dir;
import net.capps.word.game.common.Rack;
import net.capps.word.game.move.MoveType;
import net.capps.word.rest.models.MoveModel;
import net.capps.word.rest.models.PosModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by charlescapps on 1/22/15.
 */
public class MovesDAO {
    private static final MovesDAO INSTANCE = new MovesDAO();
    private static final WordDbManager WORD_DB_MANAGER = WordDbManager.getInstance();

    public static MovesDAO getInstance() {
        return INSTANCE;
    }

    private static final String INSERT_MOVE =
            "INSERT INTO word_moves (game_id, player_id, move_type, start_row, start_col, direction, word, tiles_played, points, date_played) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

    private static final String GET_RECENT_MOVES =
            "SELECT * FROM word_moves WHERE game_id = ? ORDER BY id DESC LIMIT ?";

    private static final String GET_MOVES_IN_ORDER =
            "SELECT * FROM word_moves WHERE game_id = ? ORDER BY id ASC LIMIT ?";

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

    public MoveModel insertMove(MoveModel inputMove, int numPoints) throws SQLException {
        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            return insertMove(inputMove, numPoints, dbConn);
        }
    }

    public MoveModel insertMove(MoveModel inputMove, int numPoints, Connection dbConn) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(INSERT_MOVE, Statement.RETURN_GENERATED_KEYS);
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
            throw new SQLException("There was no result returned after insterting a move!");
        }

        return getMoveFromResult(result);
    }

    public List<MoveModel> getLastMovesByPlayer(int playerId, int gameId, Connection dbConn) throws SQLException {
        List<MoveModel> recentMoves = getMostRecentMoves(gameId, Rack.MAX_TILES_IN_RACK, dbConn);
        List<MoveModel> movesByPlayer = new ArrayList<>();
        for (MoveModel move: recentMoves) {
            if (move.getPlayerId().equals(playerId)) {
                movesByPlayer.add(0, move);
            } else {
                break;
            }
        }

        return movesByPlayer;
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
