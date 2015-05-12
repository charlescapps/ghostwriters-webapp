package net.capps.word.servlet;

import net.capps.word.db.dao.UsersDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by charlescapps on 5/12/15.
 */
public class IncrementTokensRunnable implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(IncrementTokensRunnable.class);
    private static final UsersDAO usersDAO = UsersDAO.getInstance();

    @Override
    public void run() {
        try {
            doIncrementTokens();
        } catch (Exception e) {
            LOG.error("Error incrementing tokens!", e);
        }
    }

    private void doIncrementTokens() throws Exception {
        usersDAO.incrementAllUserTokens();
    }
}
