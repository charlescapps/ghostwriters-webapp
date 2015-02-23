package net.capps.word.db.dao;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import net.capps.word.db.WordDbManager;
import net.capps.word.game.board.GameState;
import net.capps.word.game.board.SquareSet;
import net.capps.word.game.board.TileSet;
import net.capps.word.game.common.*;
import net.capps.word.rest.models.GameModel;
import net.capps.word.rest.models.MoveModel;

import java.sql.*;
import java.util.Date;

/**
 * Created by charlescapps on 1/21/15.
 */
public class GamesDAO {
    private static final GamesDAO INSTANCE = new GamesDAO();

    private static final String INSERT_GAME_QUERY =
        "INSERT INTO word_games (game_type, ai_type, player1, player2, player1_rack, player2_rack, player1_points, player2_points, " +
                    "board_size, bonuses_type, game_density, squares, tiles, game_result, player1_turn, date_started)" +
        " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_GAME_QUERY =
            "UPDATE word_games SET (player1_rack, player2_rack, player1_points, player2_points, squares, tiles, game_result, player1_turn) " +
                               " = (?, ?, ?, ?, ?, ?, ?, ?) " +
            "WHERE id = ?;";

    private static final String QUERY_GAME_BY_ID =
            "SELECT * FROM word_games WHERE id = ?;";

    public static GamesDAO getInstance() {
        return INSTANCE;
    }

    public GameModel createNewGame(GameModel validatedInputGame, TileSet tileSet, SquareSet squareSet) throws Exception {
        final String squares = squareSet.toCompactString();
        final String tiles = tileSet.toCompactString();
        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            PreparedStatement stmt = dbConn.prepareStatement(INSERT_GAME_QUERY, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, validatedInputGame.getGameType().ordinal());
            final AiType aiType = validatedInputGame.getAiType();
            if (aiType != null) {
                stmt.setInt(2, aiType.ordinal());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setInt(3, validatedInputGame.getPlayer1());
            stmt.setInt(4, validatedInputGame.getPlayer2());
            stmt.setString(5, "");
            stmt.setString(6, "");
            stmt.setInt(7, 0); // Player 1 starts with 0 points
            stmt.setInt(8, 0); // Player 2 starts with 0 points
            stmt.setShort(9, (short) validatedInputGame.getBoardSize().ordinal());
            stmt.setShort(10, (short) validatedInputGame.getBonusesType().ordinal());
            stmt.setShort(11, (short) validatedInputGame.getGameDensity().ordinal());
            stmt.setString(12, squares);
            stmt.setString(13, tiles);
            stmt.setShort(14, (short) GameResult.IN_PROGRESS.ordinal());
            stmt.setBoolean(15, true); // Always starts as player 1's turn.

            Timestamp timestamp = new Timestamp(new Date().getTime());
            stmt.setTimestamp(16, timestamp);

            stmt.executeUpdate();

            // Construct the GameModel to return from the inserted data.
            ResultSet result = stmt.getGeneratedKeys();
            if (!result.next()) {
                throw new IllegalStateException("Just inserted game into database, but no result present!");
            }

            return getGameByResultSetRow(result);
        }
    }

    public Optional<GameModel> getGameById(int gameId) throws Exception {
        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            PreparedStatement stmt = dbConn.prepareStatement(QUERY_GAME_BY_ID);
            stmt.setInt(1, gameId);

            ResultSet result = stmt.executeQuery();
            if (!result.next()) {
                return Optional.absent();
            }
            return Optional.of(getGameByResultSetRow(result));
        }
    }

    public GameModel updateGame(GameState updatedGame, MoveModel validatedMove, int numPoints) throws Exception {
        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            try {
                // Set auto-commit to false, to update Games table and Moves table and rollback if anything fails.
                dbConn.setAutoCommit(false);

                PreparedStatement updateGameStmt = dbConn.prepareStatement(UPDATE_GAME_QUERY, Statement.RETURN_GENERATED_KEYS);
                updateGameStmt.setString(1, updatedGame.getPlayer1Rack().toString());
                updateGameStmt.setString(2, updatedGame.getPlayer2Rack().toString());
                updateGameStmt.setInt(3, updatedGame.getPlayer1Points());
                updateGameStmt.setInt(4, updatedGame.getPlayer2Points());
                updateGameStmt.setString(5, updatedGame.getSquareSet().toCompactString());
                updateGameStmt.setString(6, updatedGame.getTileSet().toCompactString());
                updateGameStmt.setShort(7, (short) updatedGame.getGameResult().ordinal());
                updateGameStmt.setBoolean(8, updatedGame.isPlayer1Turn());
                updateGameStmt.setInt(9, updatedGame.getGameId());

                int rowCount = updateGameStmt.executeUpdate();

                if (rowCount != 1) {
                    throw new SQLException("Row count isn't 1 after updating game, row count is: " + rowCount);
                }
                ResultSet result = updateGameStmt.getGeneratedKeys();
                result.next();

                GameModel updatedGameModel = getGameByResultSetRow(result);

                // Now insert the Move.
                MovesDAO.getInstance().insertMove(validatedMove, numPoints, dbConn);

                dbConn.commit();

                return updatedGameModel;

            } catch (Exception e) {
                dbConn.rollback();
                throw e;
            }
        }
    }

    private GameModel getGameByResultSetRow(ResultSet result) throws SQLException {
        Preconditions.checkArgument(!result.isClosed() && !result.isAfterLast() && !result.isBeforeFirst(),
                "Error - attempting to create a Game from an invalid ResultSet.");
        GameModel game = new GameModel();
        game.setId(result.getInt("id"));
        final GameType gameType = GameType.values()[result.getInt("game_type")];
        game.setGameType(gameType);
        final AiType aiType = gameType == GameType.TWO_PLAYER ? null : AiType.values()[result.getInt("ai_type")];
        game.setAiType(aiType);
        game.setPlayer1(result.getInt("player1"));
        game.setPlayer2(result.getInt("player2"));
        game.setPlayer1Rack(result.getString("player1_rack"));
        game.setPlayer2Rack(result.getString("player2_rack"));
        game.setPlayer1Points(result.getInt("player1_points"));
        game.setPlayer2Points(result.getInt("player2_points"));
        game.setBoardSize(BoardSize.values()[result.getInt("board_size")]);
        game.setBonusesType(BonusesType.values()[result.getInt("bonuses_type")]);
        game.setGameDensity(GameDensity.values()[result.getInt("game_density")]);
        game.setSquares(result.getString("squares"));
        game.setTiles(result.getString("tiles"));
        game.setGameResult(GameResult.values()[result.getShort("game_result")]);
        game.setPlayer1Turn(result.getBoolean("player1_turn"));
        Timestamp dateStarted = result.getTimestamp("date_started");
        game.setDateCreated(dateStarted.getTime());
        return game;
    }
}
