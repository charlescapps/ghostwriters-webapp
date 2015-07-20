package net.capps.word.servlet;

import net.capps.word.db.dao.GamesDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by charlescapps on 5/12/15.
 */
public class TimeOutInactiveGamesRunnable implements Runnable {
    public static final int TIME_OUT_GAME_SECONDS = (int)TimeUnit.DAYS.toSeconds(14);

    private static final Logger LOG = LoggerFactory.getLogger(TimeOutInactiveGamesRunnable.class);
    private static final GamesDAO gamesDAO = GamesDAO.getInstance();

    @Override
    public void run() {
        try {
            doExpireTimedOutGames();
        } catch (Exception e) {
            LOG.error("Error timing out games!", e);
        }
    }

    private void doExpireTimedOutGames() throws Exception {
        gamesDAO.timeoutInactiveGames(TIME_OUT_GAME_SECONDS);
    }
}
