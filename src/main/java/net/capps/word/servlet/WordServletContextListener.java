package net.capps.word.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by charlescapps on 3/15/15.
 */
public class WordServletContextListener implements ServletContextListener {
    private InitDictionariesThread initDictionariesThread = null;
    private static final Logger LOG = LoggerFactory.getLogger(WordServletContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if ((initDictionariesThread == null) || !initDictionariesThread.isAlive()) {
            LOG.info("Starting InitDictionariesThread...");
            initDictionariesThread = new InitDictionariesThread();
            initDictionariesThread.start();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce){
        try {
            initDictionariesThread.interrupt();
        } catch (Exception ex) {
        }
    }
}
