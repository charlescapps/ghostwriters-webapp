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

    public static final String CREATE_LOWERCASE_USER_IDX =
            "CREATE UNIQUE INDEX idx_username_lower ON word_users (lower(username));";

    public static final String DROP_LOWERCASE_USER_IDX =
            "DROP INDEX IF EXISTS idx_username_lower;";

    public static final String CREATE_GAMES_TABLE =
            "CREATE TABLE IF NOT EXISTS word_games " +
                    "( id SERIAL PRIMARY KEY," +
                    "player1 INTEGER NOT NULL," +
                    "player2 INTEGER NOT NULL," +
                    "player1_rack VARCHAR(20) NOT NULL," +
                    "player2_rack VARCHAR(20) NOT NULL," +
                    "player1_points INTEGER NOT NULL," +
                    "player2_points INTEGER NOT NULL," +
                    "board_size SMALLINT NOT NULL," +
                    "bonuses_type SMALLINT NOT NULL," +
                    "game_density SMALLINT NOT NULL," +
                    "squares VARCHAR(400) NOT NULL," +
                    "tiles VARCHAR(800) NOT NULL," +
                    "game_result SMALLINT NOT NULL," +
                    "player1_turn BOOLEAN NOT NULL," +
                    "date_started TIMESTAMP WITH TIME ZONE NOT NULL);";

    public static final String CREATE_MOVES_TABLE =
            "CREATE TABLE IF NOT EXISTS word_moves " +
                    "( id BIGSERIAL PRIMARY KEY," +
                      "game_id INTEGER NOT NULL," +
                      "move_type SMALLINT NOT NULL," +
                      "start_row SMALLINT NOT NULL," +
                      "start_col SMALLINT NOT NULL," +
                      "direction CHAR(1) NOT NULL," +
                      "word VARCHAR(32) NOT NULL," +
                      "tiles_played VARCHAR(16) NOT NULL," +
                      "points INTEGER NOT NULL," +
                      "date_played TIMESTAMP WITH TIME ZONE NOT NULL" +
                    ");";

    public static final String CREATE_SESSION_TABLE =
            "CREATE TABLE IF NOT EXISTS word_sessions " +
                    "( id BIGSERIAL PRIMARY KEY," +
                      "user_id INTEGER UNIQUE," +
                      "session_id VARCHAR(64) UNIQUE," +
                      "date_created TIMESTAMP NOT NULL" +
                     ");";
}
