package net.capps.word.rest.services;

import net.capps.word.RootService;
import net.capps.word.crypto.CryptoUtils;
import net.capps.word.db.dao.UsersDAO;
import net.capps.word.heroku.SetupHelper;
import net.capps.word.rest.auth.AuthHelper;
import net.capps.word.rest.models.UserModel;
import net.capps.word.rest.providers.UsersProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

/**
 * Created by charlescapps on 1/24/15.
 */
public class GamesServiceTest extends JerseyTest {

    private static final Logger LOG = LoggerFactory.getLogger(GamesServiceTest.class);

    private static UserModel fooUser;
    private static UserModel barUser;

    @Override
    protected Application configure() {
        return new ResourceConfig(RootService.class);
    }

    @BeforeClass
    public static void beforeTest() throws Exception {
        SetupHelper setupHelper = SetupHelper.getInstance();
        setupHelper.initDatabase();
        setupHelper.createInitialUser();
        setupHelper.initDictionary();
        setupHelper.initLayouts();

        // Create 2 regular users
        UserModel fooInput = new UserModel(null, "Foo", "foo@example.com", "foo", null);
        UserModel barInput = new UserModel(null, "Bar", "bar@example.com", "bar", null);
        fooUser = UsersProvider.getInstance().createNewUser(fooUser);
        barUser = UsersProvider.getInstance().createNewUser(barUser);

    }

    private String login(String username, String password) {
        Response response =
                target().path("login").request()
                .header("Authentication",CryptoUtils.getBasicAuthHeader(username, password))
                .build("POST")
                .invoke();
        return response.getHeaderString("Set-Cookie");
    }

    @Test
    public void testCreateGame() {
        String cookie = login(fooUser.getUsername(), "foo");


    }


}
