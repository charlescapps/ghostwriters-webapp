package net.capps.word.db.dao;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import net.capps.word.db.WordDbManager;
import net.capps.word.game.board.SquareSet;
import net.capps.word.game.board.TileSet;
import net.capps.word.game.common.BoardSize;
import net.capps.word.game.common.BonusesType;
import net.capps.word.game.common.GameDensity;
import net.capps.word.game.common.GameResult;
import net.capps.word.rest.models.GameModel;

import java.sql.*;
import java.util.Date;

/**
 * Created by charlescapps on 1/21/15.
 */
public class GamesDAO {
    private static final GamesDAO INSTANCE = new GamesDAO();

    private static final String INSERT_GAME_QUERY =
        "INSERT INTO word_games (player1, player2, player1_rack, player2_rack," +
                    " board_size, bonuses_type, game_density, squares, tiles, game_result, player1_turn, date_started)" +
        " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
            stmt.setInt(1, validatedInputGame.getPlayer1());
            stmt.setInt(2, validatedInputGame.getPlayer2());
            stmt.setString(3, "");
            stmt.setString(4, "");
            stmt.setShort(5, (short) validatedInputGame.getBoardSize().ordinal());
            stmt.setShort(6, (short) validatedInputGame.getBonusesType().ordinal());
            stmt.setShort(7, (short) validatedInputGame.getGameDensity().ordinal());
            stmt.setString(8, squares);
            stmt.setString(9, tiles);
            stmt.setShort(10, (short) GameResult.IN_PROGRESS.ordinal());
            stmt.setBoolean(11, true); // Always starts as player 1's turn.

            Timestamp timestamp = new Timestamp(new Date().getTime());
            stmt.setTimestamp(12, timestamp);

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

    private GameModel getGameByResultSetRow(ResultSet result) throws SQLException {
        Preconditions.checkArgument(!result.isClosed() && !result.isAfterLast() && !result.isBeforeFirst(),
                "Error - attempting to create a Game from an invalid ResultSet.");
        GameModel game = new GameModel();
        game.setId(result.getInt("id"));
        game.setPlayer1(result.getInt("player1"));
        game.setPlayer2(result.getInt("player2"));
        game.setPlayer1Rack(result.getString("player1_rack"));
        game.setPlayer2Rack(result.getString("player2_rack"));
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
