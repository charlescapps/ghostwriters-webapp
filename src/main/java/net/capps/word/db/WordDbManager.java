package net.capps.word.db;

import com.google.common.base.Strings;
import org.apache.commons.dbcp2.BasicDataSource;
import org.postgresql.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by charlescapps on 12/26/14.
 */
public class WordDbManager {
    // ------------ Static fields ---------
    private static final Logger LOG = LoggerFactory.getLogger(WordDbManager.class);
    private static final String DATABASE_URL = System.getenv("DATABASE_URL");
    private static final WordDbManager INSTANCE = new WordDbManager();
    private static final int INITIAL_POOL_SIZE = 5;

    // ------------ Private fields ------------
    private final URI DATABASE_URI;
    private final boolean USE_CONNECTION_POOL = Boolean.parseBoolean(System.getProperty("ghostwriters.useConnectionPool", "true"));

    private final String username;
    private final String password;
    private final String dbUrl;

    private final BasicDataSource connectionPool;


    private WordDbManager() {
        final String databaseUrl = getDatabaseUrl();
        DATABASE_URI = URI.create(databaseUrl);
        LOG.info("DATABASE_URL=" + DATABASE_URI);
        username = DATABASE_URI.getUserInfo().split(":")[0];
        password = DATABASE_URI.getUserInfo().split(":")[1];
        dbUrl = "jdbc:postgresql://" + DATABASE_URI.getHost() + ":" + DATABASE_URI.getPort() + DATABASE_URI.getPath();

        LOG.info("USE_CONNECTION_POOL=" + USE_CONNECTION_POOL);
        if (USE_CONNECTION_POOL) {
            LOG.info("Creating connection pool!");
            connectionPool = new BasicDataSource();
            connectionPool.setUsername(username);
            connectionPool.setPassword(password);
            connectionPool.setDriverClassName(org.postgresql.Driver.class.getName());
            connectionPool.setUrl(dbUrl);
            connectionPool.setInitialSize(INITIAL_POOL_SIZE);
            // Data gathered does not support increasing the maxIdle connections from the default of 8
            // See https://docs.google.com/spreadsheets/d/1NhEWeJMA3NJR0-1R9-VU1q2sVdJWPJDYaki-HrjjXo8/edit#gid=2108394032
        } else {
            LOG.info("NO connection pool!");
            connectionPool = null;
        }
    }

    public static WordDbManager getInstance() {
        return INSTANCE;
    }

    public String getDatabaseUrl() {
        return !Strings.isNullOrEmpty(DATABASE_URL) ? DATABASE_URL : System.getProperty("net.capps.databaseUrl");
    }

    public Connection getConnection() throws SQLException {
        if (connectionPool != null) {
            return connectionPool.getConnection();
        } else {
            return DriverManager.getConnection(dbUrl, username, password);
        }
    }
}
