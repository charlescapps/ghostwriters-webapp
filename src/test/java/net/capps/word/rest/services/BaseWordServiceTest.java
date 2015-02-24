package net.capps.word.rest.services;

import net.capps.word.crypto.CryptoUtils;
import net.capps.word.heroku.SetupHelper;
import net.capps.word.rest.models.UserModel;
import net.capps.word.rest.providers.UsersProvider;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.grizzly2.servlet.GrizzlyWebContainerFactory;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.BeforeClass;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.logging.Level;

/**
 * Created by charlescapps on 1/25/15.
 */
public class BaseWordServiceTest extends JerseyTest {
    protected static UserModel fooUser;
    protected static UserModel barUser;

    @BeforeClass
    public static void initWordServiceTest() throws Exception {
        // Set the database URL
        System.setProperty("net.capps.databaseUrl", "postgres://postgres:password@localhost:5432/wordattack");
        System.setProperty("jersey.config.test.logging.record.level", Integer.toString(Level.ALL.intValue()));
        System.setProperty("jersey.test.host", "localhost");
        System.setProperty("jersey.config.test.container.port", "8080");
        // Setup everything but Jetty.
        SetupHelper setupHelper = SetupHelper.getInstance();
        setupHelper.initDatabase();
        setupHelper.createInitialUser();
        setupHelper.initDictionaryDataStructures();
        setupHelper.initGameDataStructures();

        // Create 2 regular users
        final String fooUsername = "Foo_" + System.currentTimeMillis() / 1000;
        final String barUsername = "Bar_" + System.currentTimeMillis() / 1000;

        UserModel fooInput = new UserModel(null, fooUsername, fooUsername + "@example.com", "foo", null, false);
        UserModel barInput = new UserModel(null, barUsername, barUsername + "@example.com", "bar", null, false);
        fooUser = UsersProvider.getInstance().createNewUser(fooInput);
        barUser = UsersProvider.getInstance().createNewUser(barInput);
    }

    // ---------- Protected ---------
    protected String login(String username, String password) {
        Response response =
                target().path("login").request()
                        .header("Authorization", "Basic " + CryptoUtils.getBasicAuthHeader(username, password))
                        .build("POST")
                        .invoke();
        return response.getHeaderString("Set-Cookie");
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new TestContainerFactory() {
            @Override
            public TestContainer create(final URI baseUri, final DeploymentContext deploymentContext) throws IllegalArgumentException {
                return new TestContainer() {
                    private HttpServer server;

                    @Override
                    public ClientConfig getClientConfig() {
                        return null;
                    }

                    @Override
                    public URI getBaseUri() {
                        return baseUri;
                    }

                    @Override
                    public void start() {
                        try {
                            this.server = GrizzlyWebContainerFactory.create(
                                    baseUri, Collections.singletonMap(
                                            "jersey.config.server.provider.packages",
                                            "net.capps.word.rest")
                            );
                        } catch (ProcessingException e) {
                            throw new TestContainerException(e);
                        } catch (IOException e) {
                            throw new TestContainerException(e);
                        }
                    }

                    @Override
                    public void stop() {
                        this.server.stop();
                    }
                };

            }
        };
    }

}
