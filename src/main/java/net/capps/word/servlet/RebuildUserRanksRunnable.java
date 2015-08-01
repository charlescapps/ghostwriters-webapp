package net.capps.word.servlet;

import net.capps.word.heroku.SetupHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by charlescapps on 5/12/15.
 */
public class RebuildUserRanksRunnable implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(RebuildUserRanksRunnable.class);
    private static final SetupHelper setupHelper = SetupHelper.getInstance();

    @Override
    public void run() {
        LOG.info("[START TASK] Starting task to rebuild user ranks...");
        try {
            doRebuildUserRanks();
            LOG.info("[END TASK] SUCCESS - rebuilt user ranks!");
        } catch (Exception e) {
            LOG.error("Error rebuilding user ranks!", e);
        }
    }

    private void doRebuildUserRanks() throws Exception {
        setupHelper.initRankDataStructures();
    }
}
