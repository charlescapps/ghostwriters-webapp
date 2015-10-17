package net.capps.word.servlet;

import net.capps.word.db.dao.MovesDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by charlescapps on 5/12/15.
 */
public class DeleteOldMovesRunnable implements Runnable {
    public static final int TIME_OUT_MOVE_SECONDS = (int)TimeUnit.DAYS.toSeconds(14);

    private static final Logger LOG = LoggerFactory.getLogger(DeleteOldMovesRunnable.class);
    private static final MovesDAO movesDAO = MovesDAO.getInstance();

    @Override
    public void run() {
        try {
            doDeleteOldMoves();
        } catch (Exception e) {
            LOG.error("Error deleting old moves!", e);
        }
    }

    private void doDeleteOldMoves() throws Exception {
        movesDAO.deleteOldMoves(TIME_OUT_MOVE_SECONDS);
    }
}
