package net.capps.word.db;

/**
 * Created by charlescapps on 12/26/14.
 */
public class TableDefinitions {
    public static final String CREATE_WORD_USERS_TABLE =
            "CREATE TABLE IF NOT EXISTS word_users (" +
            "id bigserial PRIMARY KEY," +
            "username VARCHAR(16) UNIQUE NOT NULL," +
            "email VARCHAR(128) UNIQUE," +
            "hashpass VARCHAR(64) NOT NULL," +
            "salt VARCHAR(16) NOT NULL" +
            ");";
}
