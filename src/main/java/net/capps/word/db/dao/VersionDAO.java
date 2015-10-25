package net.capps.word.db.dao;

import net.capps.word.versioning.CurrentVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Optional;

import static java.lang.String.format;

/**
 * Created by charlescapps on 10/24/15.
 */
public class VersionDAO {
    private static final Logger LOG = LoggerFactory.getLogger(VersionDAO.class);
    private static final VersionDAO INSTANCE = new VersionDAO();

    private static final String GET_VERSION = "SELECT * FROM word_version_info LIMIT 1";

    private static final String INSERT_VERSION = "INSERT INTO word_version_info (version_code) VALUES (?);";

    private static final String UPDATE_VERSION =
            "UPDATE word_version_info SET version_code = ?;";

    private VersionDAO() {} // Singleton

    public static VersionDAO getInstance() {
        return INSTANCE;
    }

    public void insertCurrentVersionIfNotPresent(Connection dbConn) throws SQLException {
        Optional<Integer> versionOpt = getVersionOpt(dbConn);
        if (versionOpt.isPresent()) {
            return;
        }

        insertCurrentVersion(dbConn);
    }

    public Optional<Integer> getVersionOpt(Connection dbConn) throws SQLException {
        Statement stmt = dbConn.createStatement();
        ResultSet resultSet = stmt.executeQuery(GET_VERSION);
        if (resultSet.next()) {
            return Optional.of(resultSet.getInt("version_code"));
        }
        return Optional.empty();
    }

    public void insertCurrentVersion(Connection dbConn) throws SQLException {

        LOG.info("===== Inserting version into word_version_info equal to the current version: " + CurrentVersion.VERSION_CODE);
        PreparedStatement stmt = dbConn.prepareStatement(INSERT_VERSION);
        stmt.setInt(1, CurrentVersion.VERSION_CODE);
        int updated = stmt.executeUpdate();
        if (updated != 1) {
            throw new SQLException("Expected 1 row to be updated after inserting version into word_version_info table, but updated = " + updated);
        }
    }

    public void updateVersionToCurrentVersion(Connection dbConn) throws SQLException {
        LOG.info("==== Updating version in word_version_info to the current version: " + CurrentVersion.VERSION_CODE);
        PreparedStatement stmt = dbConn.prepareStatement(UPDATE_VERSION);
        stmt.setInt(1, CurrentVersion.VERSION_CODE);
        int updated = stmt.executeUpdate();
        if (updated != 1) {
            throw new SQLException("The number of rows updated after updating version should be 1, but updated = " + updated);
        }
    }
}
