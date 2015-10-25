package net.capps.word.db.dao;

import net.capps.word.versioning.CurrentVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import static java.lang.String.format;

/**
 * Created by charlescapps on 10/24/15.
 */
public class VersionDAO {
    private static final Logger LOG = LoggerFactory.getLogger(VersionDAO.class);
    private static final VersionDAO INSTANCE = new VersionDAO();

    private static final String GET_CURRENT_VERSION = "SELECT * FROM word_version_info LIMIT 1";

    private static final String INSERT_DEFAULT_VERSION =
            format("INSERT INTO word_version_info (version_code) VALUES(%d);", CurrentVersion.VERSION_CODE);

    public static VersionDAO getInstance() {
        return INSTANCE;
    }

    public void insertCurrentVersionIfNotPresent(Connection dbConn) throws SQLException {
        Optional<Integer> currentVersionOpt = getCurrentVersion(dbConn);
        if (currentVersionOpt.isPresent()) {
            return;
        }

        insertCurrentVersion(dbConn);
    }

    public Optional<Integer> getCurrentVersion(Connection dbConn) throws SQLException {
        Statement stmt = dbConn.createStatement();
        ResultSet resultSet = stmt.executeQuery(GET_CURRENT_VERSION);
        if (resultSet.next()) {
            return Optional.of(resultSet.getInt("version_code"));
        }
        return Optional.empty();
    }

    public void insertCurrentVersion(Connection dbConn) throws SQLException {
        Statement stmt = dbConn.createStatement();

        LOG.info("===== Inserting version into word_version_info equal to the current version: " + CurrentVersion.VERSION_CODE);
        int updated = stmt.executeUpdate(INSERT_DEFAULT_VERSION);
        if (updated != 1) {
            throw new SQLException("Expected 1 row to be updated after inserting version into word_version_info table, but updated = " + updated);
        }
    }
}
