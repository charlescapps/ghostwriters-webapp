package net.capps.word.versioning.upgrade_tasks;

import net.capps.word.versioning.UpgradeTask;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by charlescapps on 10/24/15.
 */
public class Version3AddOneSignalIdColumn implements UpgradeTask {

    public static final String ADD_ONE_SIGNAL_ID_COLUMN =
            "ALTER TABLE IF EXISTS word_users " +
            "ADD COLUMN one_signal_user_id VARCHAR(64)";

    @Override
    public void upgrade(Connection dbConn) throws SQLException {
        Statement stmt = dbConn.createStatement();
        stmt.executeUpdate(ADD_ONE_SIGNAL_ID_COLUMN);
    }

    @Override
    public int version() {
        return 3;
    }

    @Override
    public String description() {
        return "Add the 'one_signal_user_id' column to the word_users table.";
    }
}
