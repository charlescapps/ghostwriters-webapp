package net.capps.word.rest.services;

import net.capps.word.crypto.CryptoUtils;
import net.capps.word.db.WordDbManager;
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
import java.sql.Connection;
import java.util.Collections;
import java.util.logging.Level;

/**
 * Created by charlescapps on 1/25/15.
 */
public class BaseWordServiceTest extends JerseyTest {
    protected static UserModel fooUser;
    protected static UserModel barUser;
    protected static final String PASS = "abcde";

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
        setupHelper.initDictionaryDataStructures();
        setupHelper.initGameDataStructures();
        setupHelper.createAiUsers();

        // Create 2 regular users
        final String fooUsername = "Foo_" + System.currentTimeMillis() / 1000;
        final String barUsername = "Bar_" + System.currentTimeMillis() / 1000;

        UserModel fooInput = new UserModel(null, fooUsername, fooUsername + "@example.com", null, null, false);
        UserModel barInput = new UserModel(null, barUsername, barUsername + "@example.com", null, null, false);
        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            fooUser = UsersProvider.getInstance().createNewUser(dbConn, fooInput);
            barUser = UsersProvider.getInstance().createNewUser(dbConn, barInput);
            UsersProvider.getInstance().updateUserPassword(fooUser.getId(), PASS, dbConn);
            UsersProvider.getInstance().updateUserPassword(barUser.getId(), PASS, dbConn);
        }
    }

    // ---------- Protected ---------
    protected String login(String username, String password) {
        Response response =
                target().path("login").request()
                        .header("Authorization", "Basic " + CryptoUtils.getBasicAuthHeader(username, password))
                        .build("POST")
                        .invoke();
        return parseCookie(response.getHeaderString("Set-Cookie"));
    }

    protected String parseCookie(String cookie) {
        if (cookie == null) {
            return null;
        }

        int index = cookie.indexOf(';');
        if (index < 0) {
            return cookie;
        }

        return cookie.substring(0, index);
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
