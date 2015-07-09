package net.capps.word.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by charlescapps on 3/15/15.
 */
public class WordServletContextListener implements ServletContextListener {
    private InitDictionariesThread initDictionariesThread;
    private ScheduledExecutorService incrementUserTokensService;
    private static final Logger LOG = LoggerFactory.getLogger(WordServletContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (initDictionariesThread == null || !initDictionariesThread.isAlive()) {
            initializeDictionaries();
        }
        if (incrementUserTokensService == null || incrementUserTokensService.isShutdown() || incrementUserTokensService.isTerminated()) {
            startScheduledTaskToIncreaseTokens();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce){
        try {
            if (initDictionariesThread != null) {
                initDictionariesThread.interrupt();
                initDictionariesThread = null;
            }
        } catch (Exception ex) {
            LOG.error("Error shutting down InitDictionaryThread:", ex);
        }

        try {
            if (incrementUserTokensService != null) {
                incrementUserTokensService.shutdown();
                initDictionariesThread = null;
            }
        } catch (Exception ex) {
            LOG.error("Error shutting down scheduled task to increment user tokens:", ex);
        }
    }

    private void initializeDictionaries() {
        LOG.info("Starting InitDictionariesThread...");
        initDictionariesThread = new InitDictionariesThread();
        initDictionariesThread.start();
    }

    private void startScheduledTaskToIncreaseTokens() {
        LOG.info("Scheduling task to increment user tokens!");
        incrementUserTokensService = Executors.newSingleThreadScheduledExecutor();
        incrementUserTokensService.scheduleAtFixedRate(new IncrementTokensRunnable(), 1, 60, TimeUnit.MINUTES);
    }
}
