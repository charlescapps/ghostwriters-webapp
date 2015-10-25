package net.capps.word.versioning;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.capps.word.db.WordDbManager;
import net.capps.word.db.dao.VersionDAO;
import net.capps.word.versioning.upgrade_tasks.Version2UpgradeMovesTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by charlescapps on 10/24/15.
 */
public class UpgradeHelper {
    private static final UpgradeHelper INSTANCE = new UpgradeHelper();
    private static final VersionDAO versionDAO = VersionDAO.getInstance();
    private static final Logger LOG = LoggerFactory.getLogger(UpgradeHelper.class);

    private final ImmutableList<UpgradeTask> REGISTERED_UPGRADE_TASKS;

    private static final String UPGRADE_TASKS_PACKAGE = "net.capps.word.versioning.upgrade_tasks";

    private UpgradeHelper() {
        // ADD UPGRADE TASKS HERE
         REGISTERED_UPGRADE_TASKS = ImmutableList.<UpgradeTask>builder()
                 .add(new Version2UpgradeMovesTable())
                 .build();
    }

    public static UpgradeHelper getInstance() {
        return INSTANCE;
    }

    public void performNeededUpgrades() throws SQLException {
        LOG.info("==== Performing upgrade tasks as needed ====");
        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            // Get the runtime version from the word_version_info table.
            Optional<Integer> startVersionOpt = versionDAO.getVersionOpt(dbConn);

            if (!startVersionOpt.isPresent()) {
                throw new IllegalStateException("There is no version in the word_version_info table!");
            }

            final int startVersion = startVersionOpt.get();

            LOG.info("==== START VERSION: {}", startVersion);

            List<UpgradeTask> sortedTasks = getUpgradeTasksSortedByVersion();

            // Perform upgrade tasks
            boolean taskPerformed = false;
            for (UpgradeTask ut: sortedTasks) {
                if (startVersion < ut.version()) {
                    taskPerformed = true;
                    LOG.info("==== Performing upgrade task for version [{}]: \"{}\"", ut.version(), ut.description());
                    ut.upgrade(dbConn);
                }
            }

            if (!taskPerformed) {
                LOG.info("==== No upgrade tasks performed.");
            }

            // Increase the version in the database to the current version
            if (startVersion < CurrentVersion.VERSION_CODE) {
                versionDAO.updateVersionToCurrentVersion(dbConn);
                LOG.info("==== END VERSION: {}", CurrentVersion.VERSION_CODE);
            } else {
                LOG.info("==== END VERSION: {}", startVersion);
            }
        }

    }

    private List<UpgradeTask> getUpgradeTasksSortedByVersion() {
        List<UpgradeTask> copyOfUpgradeTasks = Lists.newArrayList(REGISTERED_UPGRADE_TASKS);
        Collections.sort(copyOfUpgradeTasks);
        return copyOfUpgradeTasks;
    }
}
