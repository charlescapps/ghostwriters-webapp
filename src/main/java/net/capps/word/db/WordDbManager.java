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

    public static WordDbManager getInstance() {
        return INSTANCE;
    }

    public Connection getConnection() throws URISyntaxException, SQLException {
        System.out.println("DATABASE_URL=" + DATABASE_URL); // testing logging ...
        LOG.debug("Getting connection with DATABASE_URL=" + DATABASE_URL);
        String username = DATABASE_URI.getUserInfo().split(":")[0];
        String password = DATABASE_URI.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + DATABASE_URI.getHost() + DATABASE_URI.getPath();

        return DriverManager.getConnection(dbUrl, username, password);
    }
}
