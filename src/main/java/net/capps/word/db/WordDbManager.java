package net.capps.word.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by charlescapps on 12/26/14.
 */
public class WordDbManager {
    private static final WordDbManager INSTANCE = new WordDbManager();
    private static final String DATABASE_URL = System.getenv("DATABASE_URL");
    private static final URI DATABASE_URI = URI.create(DATABASE_URL);

    private static final Logger LOG = LoggerFactory.getLogger(WordDbManager.class);

    private final String username;
    private final String password;
    private final String dbUrl;

    private WordDbManager() {
        LOG.info("DATABASE_URL=" + DATABASE_URL);
        username = DATABASE_URI.getUserInfo().split(":")[0];
        password = DATABASE_URI.getUserInfo().split(":")[1];
        dbUrl = "jdbc:postgresql://" + DATABASE_URI.getHost() + DATABASE_URI.getPath();
    }

    public static WordDbManager getInstance() {
        return INSTANCE;
    }

    public Connection getConnection() throws URISyntaxException, SQLException {
        return DriverManager.getConnection(dbUrl, username, password);
    }
}
