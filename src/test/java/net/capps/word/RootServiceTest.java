package net.capps.word;

import net.capps.word.rest.services.RootService;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.core.Application;

import static org.junit.Assert.assertEquals;

public class RootServiceTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(RootService.class);
    }

    /**
     * Test to see that the message "Got it!" is sent in the response.
     */
    @Test
    public void testGetIt() {
        final String responseMsg = target().path("test").request().get(String.class);

        assertEquals("Hello, Heroku!", responseMsg);
    }
}
