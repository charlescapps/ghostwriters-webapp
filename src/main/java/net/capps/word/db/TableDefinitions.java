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
            "CREATE TABLE IF NOT EXISTS games " +
                    "( id SERIAL PRIMARY KEY, " +
                    "  player1 INTEGER NOT NULL," +
                    "  player2 INTEGER NOT NULL," +
                    "  board_size INTEGER NOT NULL," +
                    "  bonuses_type INTEGER NOT NULL," +
                    "  game_density INTEGER NOT NULL," +
                    "  squares VARCHAR(400) NOT NULL," +
                    "  tiles varchar(400) NOT NULL," +
                    "  date_started TIMESTAMP WITH TIME ZONE NOT NULL," +
                    "  moves JSONB)";
}
