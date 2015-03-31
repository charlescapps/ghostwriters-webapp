package net.capps.word.servlet;

import org.glassfish.jersey.moxy.json.MoxyJsonConfig;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
public class MOXyJsonContextResolver implements ContextResolver<MoxyJsonConfig> {

    private final MoxyJsonConfig config;

    public MOXyJsonContextResolver() {
        config = new MoxyJsonConfig()
                .setIncludeRoot(false);
    }

    @Override
    public MoxyJsonConfig getContext(Class<?> objectType) {
        return config;
    }

}