package net.capps.word.db.dao;

import net.capps.word.db.WordDbManager;
import net.capps.word.game.board.SquareSet;
import net.capps.word.game.board.TileSet;
import net.capps.word.rest.models.GameModel;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by charlescapps on 1/21/15.
 */
public class GamesDAO {
    private static final GamesDAO INSTANCE = new GamesDAO();

    private static final String INSERT_GAME_QUERY =
        "INSERT INTO word_games (player1, player2, player1_rack, player2_rack," +
                    " board_size, bonuses_type, game_density, squares, tiles, date_started)" +
        " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static GamesDAO getInstance() {
        return INSTANCE;
    }

    public GameModel createNewGame(GameModel validatedInputGame, TileSet tileSet, SquareSet squareSet) throws Exception {
        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            dbConn.prepareStatement(INSERT_GAME_QUERY);

            return null;
        }
    }
}
