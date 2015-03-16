package net.capps.word.servlet;

import net.capps.word.heroku.SetupHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by charlescapps on 3/15/15.
 */
public class InitDictionariesThread extends Thread {
    private final static SetupHelper setupHelper = SetupHelper.getInstance();
    private final static Logger LOG = LoggerFactory.getLogger(InitDictionariesThread.class);

    public InitDictionariesThread() {
        super("Init Dictionaries Thread");
    }

    @Override
    public void run() {
        try {
            setupHelper.initDictionaryDataStructures();
            setupHelper.initGameDataStructures();
            LOG.info("SUCCESS - Finished initializing dictionary data structures and game data structures!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
