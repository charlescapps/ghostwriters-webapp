package net.capps.word.db;

import com.google.common.base.Strings;
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

    // ------------ Private fields ------------
    private final URI DATABASE_URI;

    private final String username;
    private final String password;
    private final String dbUrl;

    private WordDbManager() {
        System.out.println("HEROKU_POSTGRESQL_RED_URL: " + System.getenv("HEROKU_POSTGRESQL_RED_URL"));
        final String databaseUrl = getDatabaseUrl();
        DATABASE_URI = URI.create(databaseUrl);
        LOG.info("DATABASE_URL=" + DATABASE_URI);
        username = DATABASE_URI.getUserInfo().split(":")[0];
        password = DATABASE_URI.getUserInfo().split(":")[1];
        dbUrl = "jdbc:postgresql://" + DATABASE_URI.getHost() + DATABASE_URI.getPath();
    }

    public static WordDbManager getInstance() {
        return INSTANCE;
    }

    public String getDatabaseUrl() {
        return !Strings.isNullOrEmpty(DATABASE_URL) ? DATABASE_URL : System.getProperty("net.capps.databaseUrl");
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, username, password);
    }
}
