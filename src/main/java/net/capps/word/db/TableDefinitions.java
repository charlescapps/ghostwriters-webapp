package net.capps.word.db;

/**
 * Created by charlescapps on 12/26/14.
 */
public class TableDefinitions {
    public static final String CREATE_WORD_USERS =
            "CREATE TABLE IF NOT EXISTS word_users (" +
            "id integer PRIMARY KEY," +
            "username VARCHAR(16)," +
            "email VARCHAR(128)," +
            "hashpass BIT(160)," +
            "salt BIT(32)" +
            ")";
}
