package net.capps.word.versioning.upgrade_tasks;

import net.capps.word.versioning.UpgradeTask;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by charlescapps on 10/24/15.
 */
public class Version2UpgradeMovesTable implements UpgradeTask {

    public static final String ADD_SPECIAL_WORD_COL =
            "ALTER TABLE IF EXISTS word_moves " +
            "ADD COLUMN special_word VARCHAR(16)";

    @Override
    public void upgrade(Connection dbConn) throws SQLException {
        Statement stmt = dbConn.createStatement();
        stmt.executeUpdate(ADD_SPECIAL_WORD_COL);
    }

    @Override
    public int version() {
        return 2;
    }

    @Override
    public String description() {
        return "Add the 'special_word' column to the word_moves table so we can store special words that were perpendicular plays.";
    }
}
