package net.capps.word.servlet;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Created by charlescapps on 3/31/15.
 */
public class WordsApplication extends ResourceConfig {
    public WordsApplication() {
        packages("net.capps.word");

        register(JacksonFeature.class);

    }
}
