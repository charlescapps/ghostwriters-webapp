package net.capps.word.db.dao;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import net.capps.word.db.WordDbManager;
import net.capps.word.game.board.Game;
import net.capps.word.game.board.SquareSet;
import net.capps.word.game.board.TileSet;
import net.capps.word.game.common.*;
import net.capps.word.game.dict.SpecialDict;
import net.capps.word.rest.models.GameModel;
import net.capps.word.rest.models.MoveModel;
import net.capps.word.rest.models.UserModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by charlescapps on 1/21/15.
 */
public class GamesDAO {
    private static final GamesDAO INSTANCE = new GamesDAO();
    private static final WordDbManager WORD_DB_MANAGER = WordDbManager.getInstance();

    private static final String INSERT_GAME_QUERY =
            "INSERT INTO word_games (game_type, ai_type, special_dict, player1, player2, player1_rack, player2_rack, player1_points, player2_points, " +
                    "board_size, bonuses_type, game_density, squares, tiles, game_result, player1_turn, last_activity, date_started)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_GAME_QUERY =
            "UPDATE word_games SET (player1_rack, player2_rack, player1_points, player2_points, squares, tiles, game_result, player1_turn, move_num, last_activity) " +
                    " = (?, ?, ?, ?, ?, ?, ?, ?, move_num + 1, ?) " +
                    "WHERE id = ?;";

    private static final String UPDATE_GAME_RESULT =
            "UPDATE word_games SET game_result = ? WHERE id = ?;";

    private static final String UPDATE_PLAYER_RATING_INCREASES =
            "UPDATE word_games SET (player1_rating_increase, player2_rating_increase) = (?, ?) WHERE id = ?;";

    private static final String QUERY_GAME_BY_ID =
            "SELECT * FROM word_games WHERE id = ?;";

    private static final String SELECT_GAMES_WITH_JOIN_ON_PLAYERS =
            "SELECT word_games.*, " +
                    "p1.username AS p1_username, p1.email AS p1_email, p1.is_system_user AS p1_is_system_user, p1.date_joined AS p1_date_joined, p1.rating AS p1_rating, " +
                    "p1.wins AS p1_wins, p1.losses AS p1_losses, p1.ties AS p1_ties, " +
                    "p1.tokens AS p1_tokens, p1.infinite_books AS p1_infinite_books, " +
                    "p2.username AS p2_username, p2.email AS p2_email, p2.is_system_user AS p2_is_system_user, p2.date_joined AS p2_date_joined, p2.rating AS p2_rating, " +
                    "p2.wins AS p2_wins, p2.losses AS p2_losses, p2.ties AS p2_ties, " +
                    "p2.tokens AS p2_tokens, p2.infinite_books AS p2_infinite_books " +
                    "FROM word_games JOIN word_users AS p1 ON (player1 = p1.id) " +
                    "JOIN word_users AS p2 ON (player2 = p2.id) ";

    private static final String QUERY_GAME_BY_ID_WITH_PLAYERS =
            SELECT_GAMES_WITH_JOIN_ON_PLAYERS +
                    "WHERE word_games.id = ?;";

    private static final String QUERY_IN_PROGRESS_GAMES_LAST_ACTIVITY_DESC =
            SELECT_GAMES_WITH_JOIN_ON_PLAYERS +
                    "WHERE (player1 = ? OR player2 = ?) AND game_result = ? OR player1 = ? AND game_result = ? " +
                    "ORDER BY (player1 = ? AND player1_turn = TRUE OR player2 = ? AND player1_turn = FALSE) DESC, last_activity DESC LIMIT ?;";

    private static final String QUERY_FINISHED_GAMES_LAST_ACTIVITY_DESC =
            SELECT_GAMES_WITH_JOIN_ON_PLAYERS +
                    "WHERE (player1 = ? OR player2 = ?) AND game_result > ? ORDER BY last_activity DESC LIMIT ?;";

    private static final String QUERY_GAMES_OFFERED_TO_USER_LAST_ACTIVITY_DESC =
            SELECT_GAMES_WITH_JOIN_ON_PLAYERS +
                    "WHERE player2 = ? AND game_result = ? AND player1_turn = FALSE ORDER BY last_activity DESC LIMIT ?;";

    private static final String QUERY_GAMES_OFFERED_BY_USER_LAST_ACTIVITY_DESC =
            SELECT_GAMES_WITH_JOIN_ON_PLAYERS +
                    "WHERE player1 = ? AND game_result = ? ORDER BY last_activity DESC LIMIT ?;";

    public static GamesDAO getInstance() {
        return INSTANCE;
    }

    public GameModel createNewGame(Connection dbConn, GameModel validatedInputGame, TileSet tileSet, SquareSet squareSet) throws Exception {
        final String squares = squareSet.toCompactString();
        final String tiles = tileSet.toCompactString();
        PreparedStatement stmt = dbConn.prepareStatement(INSERT_GAME_QUERY, Statement.RETURN_GENERATED_KEYS);
        stmt.setShort(1, (short)validatedInputGame.getGameType().ordinal());
        final AiType aiType = validatedInputGame.getAiType();
        if (aiType != null) {
            stmt.setShort(2, (short) aiType.ordinal());
        } else {
            stmt.setNull(2, Types.SMALLINT);
        }
        final SpecialDict specialDict = validatedInputGame.getSpecialDict();
        if (specialDict != null) {
            stmt.setShort(3, (short)specialDict.ordinal());
        } else {
            stmt.setNull(3, Types.SMALLINT);
        }
        stmt.setInt(4, validatedInputGame.getPlayer1());
        stmt.setInt(5, validatedInputGame.getPlayer2());
        stmt.setString(6, Strings.isNullOrEmpty(validatedInputGame.getPlayer1Rack()) ? "" : validatedInputGame.getPlayer1Rack());
        stmt.setString(7, "");
        stmt.setInt(8, 0); // Player 1 starts with 0 points
        stmt.setInt(9, 0); // Player 2 starts with 0 points
        stmt.setShort(10, (short) validatedInputGame.getBoardSize().ordinal());
        stmt.setShort(11, (short) validatedInputGame.getBonusesType().ordinal());
        stmt.setShort(12, (short) validatedInputGame.getGameDensity().ordinal());
        stmt.setString(13, squares);
        stmt.setString(14, tiles);
        // Single player --> Game is immediately IN_PROGRESS
        // Two player --> Game starts in OFFERED state
        GameResult initialGameResult = validatedInputGame.getGameType() == GameType.SINGLE_PLAYER ?
                GameResult.IN_PROGRESS :
                GameResult.OFFERED;
        stmt.setShort(15, (short) initialGameResult.ordinal());
        stmt.setBoolean(16, true); // Always starts as player 1's turn.

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        stmt.setTimestamp(17, timestamp);
        stmt.setTimestamp(18, timestamp);

        stmt.executeUpdate();

        // Construct the GameModel to return from the inserted data.
        ResultSet result = stmt.getGeneratedKeys();
        if (!result.next()) {
            throw new IllegalStateException("Just inserted game into database, but no result present!");
        }

        return getGameByResultSetRow(result);
    }

    public Optional<GameModel> getGameById(int gameId) throws SQLException {
        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            return getGameById(gameId, dbConn);
        }
    }

    public Optional<GameModel> getGameById(int gameId, Connection dbConn) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(QUERY_GAME_BY_ID);
        stmt.setInt(1, gameId);

        ResultSet result = stmt.executeQuery();
        if (!result.next()) {
            return Optional.empty();
        }
        return Optional.of(getGameByResultSetRow(result));
    }

    public Optional<GameModel> getGameWithPlayerModelsById(int gameId) throws Exception {
        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            return getGameWithPlayerModelsById(gameId, dbConn);
        }
    }

    public Optional<GameModel> getGameWithPlayerModelsById(int gameId, Connection dbConn) throws Exception {
        PreparedStatement stmt = dbConn.prepareStatement(QUERY_GAME_BY_ID_WITH_PLAYERS);
        stmt.setInt(1, gameId);

        ResultSet result = stmt.executeQuery();
        if (!result.next()) {
            return Optional.empty();
        }
        return Optional.of(getGameWithPlayersByResultSetRow(result));
    }

    public GameModel updateGame(Game updatedGame, MoveModel validatedMove, int numPoints, Connection dbConn) throws Exception {
        try {
            PreparedStatement updateGameStmt = dbConn.prepareStatement(UPDATE_GAME_QUERY, Statement.RETURN_GENERATED_KEYS);
            updateGameStmt.setString(1, updatedGame.getPlayer1Rack().toString());
            updateGameStmt.setString(2, updatedGame.getPlayer2Rack().toString());
            updateGameStmt.setInt(3, updatedGame.getPlayer1Points());
            updateGameStmt.setInt(4, updatedGame.getPlayer2Points());
            updateGameStmt.setString(5, updatedGame.getSquareSet().toCompactString());
            updateGameStmt.setString(6, updatedGame.getTileSet().toCompactString());
            updateGameStmt.setShort(7, (short) updatedGame.getGameResult().ordinal());
            updateGameStmt.setBoolean(8, updatedGame.isPlayer1Turn());
            updateGameStmt.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
            updateGameStmt.setInt(10, updatedGame.getGameId());

            int rowCount = updateGameStmt.executeUpdate();

            if (rowCount != 1) {
                throw new SQLException("Row count isn't 1 after updating game, row count is: " + rowCount);
            }
            ResultSet result = updateGameStmt.getGeneratedKeys();
            result.next();

            GameModel updatedGameModel = getGameByResultSetRow(result);

            // Now insert the Move.
            MovesDAO.getInstance().insertMove(validatedMove, numPoints, dbConn);

            return updatedGameModel;

        } catch (Exception e) {
            dbConn.rollback();
            throw e;
        }
    }

    public void acceptGame(int gameId, Connection dbConn) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(UPDATE_GAME_RESULT);
        stmt.setShort(1, (short) GameResult.IN_PROGRESS.ordinal());
        stmt.setInt(2, gameId);

        int numUpdated = stmt.executeUpdate();

        if (numUpdated != 1) {
            throw new SQLException("Expected 1 row to be updated when accepting game, but the number updated = " + numUpdated);
        }
    }

    public void rejectGame(int gameId, Connection dbConn) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(UPDATE_GAME_RESULT);
        stmt.setShort(1, (short) GameResult.REJECTED.ordinal());
        stmt.setInt(2, gameId);

        int numUpdated = stmt.executeUpdate();

        if (numUpdated != 1) {
            throw new SQLException("Expected 1 row to be updated when rejecting game, but the number updated = " + numUpdated);
        }
    }

    public void updateGamePlayerRatingIncreases(int gameId, int player1RatingIncrease, int player2RatingIncrease, Connection dbConn) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(UPDATE_PLAYER_RATING_INCREASES);
        stmt.setInt(1, player1RatingIncrease);
        stmt.setInt(2, player2RatingIncrease);
        stmt.setInt(3, gameId);

        int numUpdated = stmt.executeUpdate();

        if (numUpdated != 1) {
            throw new SQLException("Expected 1 row to be updated when updating game, but the number updated = " + numUpdated);
        }
    }

    public List<GameModel> getInProgressGamesForUserDateStartedDesc(int userId, int count) throws SQLException {
        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            return getInProgressGamesForUserLastActivityDesc(userId, count, dbConn);
        }
    }

    public List<GameModel> getInProgressGamesForUserLastActivityDesc(int userId, int count, Connection dbConn) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(QUERY_IN_PROGRESS_GAMES_LAST_ACTIVITY_DESC);
        stmt.setInt(1, userId);
        stmt.setInt(2, userId);
        stmt.setInt(3, GameResult.IN_PROGRESS.ordinal());
        stmt.setInt(4, userId);
        stmt.setInt(5, GameResult.OFFERED.ordinal());
        stmt.setInt(6, userId);
        stmt.setInt(7, userId);
        stmt.setInt(8, count);

        ResultSet resultSet = stmt.executeQuery();

        List<GameModel> games = new ArrayList<>();
        while (resultSet.next()) {
            games.add(getGameWithPlayersByResultSetRow(resultSet));
        }

        return games;
    }

    public List<GameModel> getGamesOfferedToUserLastActivityDesc(int userId, int count, Connection dbConn) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(QUERY_GAMES_OFFERED_TO_USER_LAST_ACTIVITY_DESC);
        stmt.setInt(1, userId);
        stmt.setInt(2, GameResult.OFFERED.ordinal());
        stmt.setInt(3, count);

        ResultSet resultSet = stmt.executeQuery();

        List<GameModel> games = new ArrayList<>();
        while (resultSet.next()) {
            games.add(getGameWithPlayersByResultSetRow(resultSet));
        }

        return games;
    }

    public List<GameModel> getGamesOfferedByUserLastActivityDesc(int userId, int count, Connection dbConn) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(QUERY_GAMES_OFFERED_BY_USER_LAST_ACTIVITY_DESC);
        stmt.setInt(1, userId);
        stmt.setInt(2, GameResult.OFFERED.ordinal());
        stmt.setInt(3, count);

        ResultSet resultSet = stmt.executeQuery();

        List<GameModel> games = new ArrayList<>();
        while (resultSet.next()) {
            games.add(getGameWithPlayersByResultSetRow(resultSet));
        }

        return games;
    }

    public List<GameModel> getFinishedGamesForUserDateStartedDesc(int userId, int count) throws SQLException {
        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            return getFinishedGamesForUserLastActivityDesc(userId, count, dbConn);
        }
    }

    public List<GameModel> getFinishedGamesForUserLastActivityDesc(int userId, int count, Connection dbConn) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(QUERY_FINISHED_GAMES_LAST_ACTIVITY_DESC);
        stmt.setInt(1, userId);
        stmt.setInt(2, userId);
        stmt.setInt(3, GameResult.REJECTED.ordinal());
        stmt.setInt(4, count);

        ResultSet resultSet = stmt.executeQuery();

        List<GameModel> games = new ArrayList<>(count);
        while (resultSet.next()) {
            games.add(getGameWithPlayersByResultSetRow(resultSet));
        }

        return games;
    }

    // ---------------- Private ----------------
    private GameModel getGameByResultSetRow(ResultSet result) throws SQLException {
        Preconditions.checkArgument(!result.isClosed() && !result.isAfterLast() && !result.isBeforeFirst(),
                "Error - attempting to create a Game from an invalid ResultSet.");
        GameModel game = new GameModel();
        game.setId(result.getInt("id"));
        final GameType gameType = GameType.values()[result.getShort("game_type")];
        game.setGameType(gameType);
        final AiType aiType = gameType == GameType.TWO_PLAYER ? null : AiType.values()[result.getShort("ai_type")];
        game.setAiType(aiType);
        Object specialDictOrdinal = result.getObject("special_dict");
        final SpecialDict specialDict = specialDictOrdinal == null ? null : SpecialDict.values() [(Integer) specialDictOrdinal];
        game.setSpecialDict(specialDict);
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
        game.setMoveNum(result.getInt("move_num"));
        Timestamp lastActivity = result.getTimestamp("last_activity");
        Timestamp dateStarted = result.getTimestamp("date_started");
        game.setLastActivity(lastActivity.getTime());
        game.setDateCreated(dateStarted.getTime());

        Object player1RatingIncrease = result.getObject("player1_rating_increase");
        Object player2RatingIncrease = result.getObject("player2_rating_increase");
        if (player1RatingIncrease instanceof Integer) {
            game.setPlayer1RatingIncrease((Integer) player1RatingIncrease);
        }
        if (player2RatingIncrease instanceof Integer) {
            game.setPlayer2RatingIncrease((Integer) player2RatingIncrease);
        }

        return game;
    }

    private GameModel getGameWithPlayersByResultSetRow(ResultSet result) throws SQLException {
        GameModel game = getGameByResultSetRow(result);
        int p1Id = result.getInt("player1");
        String p1Username = result.getString("p1_username");
        String p1Email = result.getString("p1_email");
        Timestamp p1DateJoined = result.getTimestamp("p1_date_joined");
        boolean p1IsSystemUser = result.getBoolean("p1_is_system_user");
        int p1Rating = result.getInt("p1_rating");
        int p1Wins = result.getInt("p1_wins");
        int p1Losses = result.getInt("p1_losses");
        int p1Ties = result.getInt("p1_ties");
        int p1Tokens = result.getInt("p1_tokens");
        boolean p1InfiniteBooks = result.getBoolean("p1_infinite_books");

        int p2Id = result.getInt("player2");
        String p2Username = result.getString("p2_username");
        String p2Email = result.getString("p2_email");
        Timestamp p2DateJoined = result.getTimestamp("p2_date_joined");
        boolean p2IsSystemUser = result.getBoolean("p2_is_system_user");
        int p2Rating = result.getInt("p2_rating");
        int p2Wins = result.getInt("p2_wins");
        int p2Losses = result.getInt("p2_losses");
        int p2Ties = result.getInt("p2_ties");
        int p2Tokens = result.getInt("p2_tokens");
        boolean p2InfiniteBooks = result.getBoolean("p2_infinite_books");
        Object player1RatingIncrease = result.getObject("player1_rating_increase");
        Object player2RatingIncrease = result.getObject("player2_rating_increase");

        UserModel player1Model = new UserModel(p1Id, p1Username, p1Email, null, null, p1IsSystemUser);
        player1Model.setDateJoined(p1DateJoined.getTime());
        player1Model.setRating(p1Rating);
        player1Model.setWins(p1Wins);
        player1Model.setLosses(p1Losses);
        player1Model.setTies(p1Ties);
        player1Model.setTokens(p1Tokens);
        player1Model.setInfiniteBooks(p1InfiniteBooks);

        UserModel player2Model = new UserModel(p2Id, p2Username, p2Email, null, null, p2IsSystemUser);
        player2Model.setDateJoined(p2DateJoined.getTime());
        player2Model.setRating(p2Rating);
        player2Model.setWins(p2Wins);
        player2Model.setLosses(p2Losses);
        player2Model.setTies(p2Ties);
        player2Model.setTokens(p2Tokens);
        player2Model.setInfiniteBooks(p2InfiniteBooks);

        game.setPlayer1Model(player1Model);
        game.setPlayer2Model(player2Model);

        if (player1RatingIncrease instanceof Integer) {
            game.setPlayer1RatingIncrease((Integer) player1RatingIncrease);
        }
        if (player2RatingIncrease instanceof Integer) {
            game.setPlayer2RatingIncrease((Integer) player2RatingIncrease);
        }

        return game;
    }
}
