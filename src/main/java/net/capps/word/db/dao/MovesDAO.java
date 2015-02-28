package net.capps.word.db.dao;

import com.google.common.collect.Lists;
import net.capps.word.db.WordDbManager;
import net.capps.word.game.common.Dir;
import net.capps.word.game.move.MoveType;
import net.capps.word.rest.models.MoveModel;
import net.capps.word.rest.models.PosModel;

import java.sql.*;
import java.util.Date;
import java.util.List;

/**
 * Created by charlescapps on 1/22/15.
 */
public class MovesDAO {
    private static final MovesDAO INSTANCE = new MovesDAO();

    public static MovesDAO getInstance() {
        return INSTANCE;
    }

    private static final String INSERT_MOVE =
            "INSERT INTO word_moves (game_id, move_type, start_row, start_col, direction, word, tiles_played, points, date_played) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";

    private static final String GET_RECENT_MOVES =
            "SELECT * FROM word_moves WHERE game_id = ? ORDER BY id DESC LIMIT ?";

    public List<MoveModel> getMostRecentMoves(int gameId, int limit) throws SQLException {
        try(Connection dbConn = WordDbManager.getInstance().getConnection()) {
            PreparedStatement stmt = dbConn.prepareStatement(GET_RECENT_MOVES);
            stmt.setInt(1, gameId);
            stmt.setInt(2, limit);
            ResultSet resultSet = stmt.executeQuery();
            List<MoveModel> moves = Lists.newArrayList();
            while (resultSet.next()) {
                moves.add(getMoveFromResult(resultSet));
            }
            return moves;
        }
    }

    public MoveModel insertMove(MoveModel inputMove, int numPoints) throws SQLException {
        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            return insertMove(inputMove, numPoints, dbConn);
        }
    }

    public MoveModel insertMove(MoveModel inputMove, int numPoints, Connection dbConn) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(INSERT_MOVE, Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, inputMove.getGameId());
        stmt.setShort(2, (short) inputMove.getMoveType().ordinal());
        stmt.setShort(3, (short) inputMove.getStart().getR());
        stmt.setShort(4, (short) inputMove.getStart().getC());
        stmt.setString(5, inputMove.getDir().toString());
        stmt.setString(6, inputMove.getLetters());
        stmt.setString(7, inputMove.getTiles());
        stmt.setInt(8, numPoints);
        stmt.setTimestamp(9, new Timestamp(new Date().getTime()));

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

    private MoveModel getMoveFromResult(ResultSet result) throws SQLException {
        return new MoveModel(result.getInt("game_id"),
                MoveType.values()[result.getShort("move_type")],
                result.getString("word"),
                new PosModel(result.getShort("start_row"), result.getShort("start_col")),
                Dir.valueOf(result.getString("direction")),
                result.getString("tiles_played"),
                result.getInt("points"),
                result.getTimestamp("date_played").getTime());
    }


}
