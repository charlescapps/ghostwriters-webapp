package net.capps.word.db;

/**
 * Created by charlescapps on 12/26/14.
 */
public class TableDefinitions {
    public static final String CREATE_WORD_USERS_TABLE =
            "CREATE TABLE IF NOT EXISTS word_users (" +
            "id serial PRIMARY KEY," +
            "username VARCHAR(16) UNIQUE NOT NULL," +
            "email VARCHAR(128) UNIQUE," +
            "hashpass VARCHAR(64) NOT NULL," +
            "salt VARCHAR(16) NOT NULL," +
            "date_joined TIMESTAMP WITH TIME ZONE NOT NULL" +
            ");";

    public static final String CREATE_GAMES_TABLE =
            "CREATE TABLE IF NOT EXISTS word_games " +
                    "( id SERIAL PRIMARY KEY," +
                    "player1 INTEGER NOT NULL," +
                    "player2 INTEGER NOT NULL," +
                    "player1_rack VARCHAR(16) NOT NULL," +
                    "player2_rack VARCHAR(16) NOT NULL," +
                    "board_size SMALLINT NOT NULL," +
                    "bonuses_type SMALLINT NOT NULL," +
                    "game_density SMALLINT NOT NULL," +
                    "squares VARCHAR(400) NOT NULL," +
                    "tiles VARCHAR(800) NOT NULL," +
                    "game_result SMALLINT NOT NULL," +
                    "date_started TIMESTAMP WITH TIME ZONE NOT NULL);";

    public static final String CREATE_MOVES_TABLE =
            "CREATE TABLE IF NOT EXISTS word_moves " +
                    "( id BIGSERIAL PRIMARY KEY," +
                    "  game_id INTEGER NOT NULL," +
                    "  move_type SMALLINT NOT NULL," +
                    "  start_row SMALLINT NOT NULL," +
                    "  start_col SMALLINT NOT NULL," +
                    "  direction CHAR(1) NOT NULL," +
                    "  word VARCHAR(32)," +
                    "  tiles_played VARCHAR(16)," +
                    "  date_played TIMESTAMP WITH TIME ZONE NOT NULL" +
                    ");";
}
