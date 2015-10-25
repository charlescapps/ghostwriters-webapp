package net.capps.word.db;

import net.capps.word.versioning.CurrentVersion;

import static java.lang.String.format;

/**
 * Created by charlescapps on 12/26/14.
 */
public class TableDefinitions {
    // -------- Version Info table --------
    public static final String CREATE_VERSION_INFO_TABLE =
            format("CREATE TABLE IF NOT EXISTS word_version_info (" +
                    "id SERIAL PRIMARY KEY, " +
                    "version_code INTEGER NOT NULL DEFAULT %d);", CurrentVersion.VERSION_CODE);

    public static final String CREATE_VERSION_INDEX =
            "CREATE UNIQUE INDEX db_version_one_row ON word_version_info((version_code IS NOT NULL));";

    public static final String DROP_VERSION_IDX =
            "DROP INDEX IF EXISTS db_version_one_row;";


    // -------- Users table ---------
    public static final String CREATE_WORD_USERS_TABLE =
            "CREATE TABLE IF NOT EXISTS word_users (" +
            "id serial PRIMARY KEY," +
            "username VARCHAR(16) UNIQUE NOT NULL," +
            "email VARCHAR(128) UNIQUE," +
            "device_id VARCHAR(64) UNIQUE," +
            "hashpass VARCHAR(64)," +
            "salt VARCHAR(16)," +
            "date_joined TIMESTAMP WITH TIME ZONE NOT NULL, " +
            "is_system_user BOOLEAN NOT NULL," +
            "rating INTEGER NOT NULL," +
            "wins INTEGER NOT NULL DEFAULT 0," +
            "losses INTEGER NOT NULL DEFAULT 0," +
            "ties INTEGER NOT NULL DEFAULT 0," +
            "tokens INTEGER NOT NULL DEFAULT 10," +
            "infinite_books BOOLEAN NOT NULL DEFAULT FALSE," +
            "one_signal_user_id VARCHAR(64)" +
            ");";

    public static final String CREATE_DEVICE_ID_IDX =
            "CREATE UNIQUE INDEX idx_device_id ON word_users (device_id);";

    public static final String DROP_DEVICE_ID_IDX =
            "DROP INDEX IF EXISTS idx_device_id;";

    public static final String CREATE_USERNAME_IDX =
            "CREATE UNIQUE INDEX idx_username ON word_users (username);";

    public static final String DROP_USERNAME_IDX =
            "DROP INDEX IF EXISTS idx_username;";

    public static final String CREATE_LOWERCASE_USERNAME_IDX =
            "CREATE UNIQUE INDEX idx_username_lower ON word_users (lower(username));";

    public static final String DROP_LOWERCASE_USERNAME_IDX =
            "DROP INDEX IF EXISTS idx_username_lower;";

    public static final String CREATE_USER_RATING_IDX =
            "CREATE INDEX idx_rating ON word_users (rating);";

    public static final String DROP_USER_RATING_IDX =
            "DROP INDEX IF EXISTS idx_rating;";

    public static final String CREATE_RATING_DESC_AND_ID_IDX =
            "CREATE INDEX idx_rating_desc_and_id ON word_users (rating DESC, id);";

    public static final String DROP_RATING_DESC_AND_ID_IDX =
            "DROP INDEX IF EXISTS idx_rating_desc_and_id;";

    // --------- Games table -----------

    public static final String CREATE_GAMES_TABLE =
            "CREATE TABLE IF NOT EXISTS word_games " +
                    "( id SERIAL PRIMARY KEY," +
                    "game_type SMALLINT NOT NULL, " +
                    "special_dict SMALLINT, " +
                    "ai_type SMALLINT, " +
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
                    "move_num INTEGER NOT NULL DEFAULT 1," +
                    "player1_rating_increase INTEGER," +
                    "player2_rating_increase INTEGER," +
                    "last_activity TIMESTAMP WITH TIME ZONE NOT NULL," +
                    "date_started TIMESTAMP WITH TIME ZONE NOT NULL);";

    public static final String CREATE_PLAYER1_IDX =
            "CREATE INDEX idx_games_player1 ON word_games (player1);";

    public static final String DROP_PLAYER1_IDX =
            "DROP INDEX IF EXISTS idx_games_player1;";

    public static final String CREATE_PLAYER2_IDX =
            "CREATE INDEX idx_games_player2 ON word_games (player2);";

    public static final String DROP_PLAYER2_IDX =
            "DROP INDEX IF EXISTS idx_games_player2;";

    public static final String CREATE_LAST_ACTIVITY_IDX =
            "CREATE INDEX idx_games_last_activity ON word_games (last_activity DESC);";

    public static final String DROP_LAST_ACTIVITY_IDX =
            "DROP INDEX IF EXISTS idx_games_last_activity;";

    public static final String CREATE_MOVES_TABLE =
            "CREATE TABLE IF NOT EXISTS word_moves " +
                    "( id BIGSERIAL PRIMARY KEY," +
                      "game_id INTEGER NOT NULL," +
                      "player_id INTEGER NOT NULL," +
                      "move_type SMALLINT NOT NULL," +
                      "start_row SMALLINT NOT NULL," +
                      "start_col SMALLINT NOT NULL," +
                      "direction CHAR(1) NOT NULL," +
                      "word VARCHAR(32) NOT NULL," +
                      "tiles_played VARCHAR(16) NOT NULL," +
                      "points INTEGER NOT NULL," +
                      "date_played TIMESTAMP WITH TIME ZONE NOT NULL," +
                      "special_word VARCHAR(16)" +
                    ");";

    public static final String CREATE_GAME_ID_FOR_MOVES_TABLE_IDX =
            "CREATE INDEX idx_moves_game_id ON word_moves (game_id);";

    public static final String DROP_GAME_ID_FOR_MOVES_TABLE_IDX =
            "DROP INDEX IF EXISTS idx_moves_game_id;";

    public static final String CREATE_SESSION_TABLE =
            "CREATE TABLE IF NOT EXISTS word_sessions " +
                    "( id BIGSERIAL PRIMARY KEY," +
                      "user_id INTEGER UNIQUE," +
                      "session_id VARCHAR(64) UNIQUE," +
                      "date_created TIMESTAMP NOT NULL" +
                     ");";

    public static final String CREATE_PLAYED_WORDS_TABLE =
            "CREATE TABLE IF NOT EXISTS played_words " +
                    "( id SERIAL PRIMARY KEY," +
                    "user_id INTEGER NOT NULL," +
                    "special_dict SMALLINT NOT NULL," +
                    "word_map bytea NOT NULL, " +
                    "UNIQUE (user_id, special_dict));";

    public static final String CREATE_PLAYED_WORDS_USER_ID_IDX =
            "CREATE INDEX idx_played_words_user_id ON played_words (user_id);";

    public static final String DROP_PLAYED_WORDS_USER_ID_IDX =
            "DROP INDEX IF EXISTS idx_played_words_user_id;";
}
