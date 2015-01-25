package net.capps.word.db.dao;

import net.capps.word.db.WordDbManager;
import net.capps.word.game.common.Dir;
import net.capps.word.game.common.Pos;
import net.capps.word.game.move.MoveType;
import net.capps.word.rest.models.MoveModel;

import java.sql.*;
import java.util.Date;

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

    public MoveModel insertMove(MoveModel inputMove, int numPoints) throws Exception {
        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            return insertMove(inputMove, numPoints, dbConn);
        }
    }

    public MoveModel insertMove(MoveModel inputMove, int numPoints, Connection dbConn) throws Exception {
        PreparedStatement stmt = dbConn.prepareStatement(INSERT_MOVE, Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, inputMove.getGameId());
        stmt.setShort(2, (short) inputMove.getMoveType().ordinal());
        stmt.setShort(3, (short) inputMove.getStart().r);
        stmt.setShort(4, (short) inputMove.getStart().c);
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
                Pos.of(result.getShort("start_row"), result.getShort("start_col")),
                Dir.valueOf(result.getString("direction")),
                result.getString("tiles_played"),
                result.getInt("points"),
                result.getTimestamp("date_played").getTime());
    }


}
