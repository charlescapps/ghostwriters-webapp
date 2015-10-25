package net.capps.word.versioning;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by charlescapps on 10/24/15.
 */
public interface UpgradeTask extends Comparable<UpgradeTask> {
    /**
     * The work to be performed - pass in connection so we could share them if desired.
     */
    void upgrade(Connection dbConn) throws SQLException;

    /**
     * The version requiring this upgrade task to be performed.
     */
    int version();

    /**
     * Friendly description of the UpgradeTask.
     */
    String description();

    /**
     * Sort by version requiring the upgrade.
     */
    @Override
    public default int compareTo(UpgradeTask ut) {
        return version() - ut.version();
    }
}
