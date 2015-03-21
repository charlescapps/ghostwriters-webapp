package net.capps.word.rest.services;

import net.capps.word.rest.filters.InitialUserAuthFilter;
import net.capps.word.rest.filters.RegularUserAuthFilter;
import net.capps.word.rest.models.GameListModel;
import net.capps.word.rest.models.UserListModel;
import net.capps.word.rest.models.UserModel;
import net.capps.word.rest.providers.UsersProvider;
import org.apache.commons.lang3.RandomStringUtils;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by charlescapps on 2/1/15.
 */
public class UsersServiceTest extends BaseWordServiceTest {
    private static final Logger LOG = LoggerFactory.getLogger(UsersServiceTest.class);

    private static UserModel user1;
    private static UserModel user2;

    @Override
    protected Application configure() {
        return new ResourceConfig(LoginService.class, GamesService.class, MovesService.class, UsersService.class,
                RegularUserAuthFilter.class, InitialUserAuthFilter.class);
    }

    @BeforeClass
    public static void createUsers() throws Exception {
        final String username1 = RandomStringUtils.randomAlphanumeric(8);
        final String username2 = username1 + RandomStringUtils.randomAlphanumeric(8);
        UserModel user1Input = new UserModel(null, username1, username1 + "@example.com", null, null, false);
        UserModel user2Input = new UserModel(null, username2, username2 + "@example.com", null, null, false);
        user1 = UsersProvider.getInstance().createNewUser(user1Input);
        user2 = UsersProvider.getInstance().createNewUser(user2Input);
    }

    @Test
    public void testSearchUsersByExactName() {
        String cookie = login(fooUser.getUsername(), "foo");
        LOG.info("Cookie={}", cookie);

        UserListModel userListModel = target("/users")
                .queryParam("q", user2.getUsername())
                .queryParam("maxResults", "10")
                .request()
                .header("Cookie", cookie)
                .accept(MediaType.APPLICATION_JSON)
                .get(UserListModel.class);

        List<UserModel> users = userListModel.getList();
        Assert.assertEquals(1, users.size());
        Assert.assertEquals(user2, users.get(0));
    }

    @Test
    public void testSearchUsersCaseInsensitive() {
        String cookie = login(fooUser.getUsername(), "foo");
        LOG.info("Cookie={}", cookie);

        UserListModel gameListModel = target("/users")
                .queryParam("q", user2.getUsername().toLowerCase())
                .queryParam("maxResults", "10")
                .request()
                .header("Cookie", cookie)
                .accept(MediaType.APPLICATION_JSON)
                .get(UserListModel.class);

        List<UserModel> users = gameListModel.getList();
        Assert.assertEquals(1, users.size());
        Assert.assertEquals(user2, users.get(0));
    }

    @Test
    public void testSearchUsersBySubstring() {
        String cookie = login(fooUser.getUsername(), "foo");
        LOG.info("Cookie={}", cookie);

        UserListModel gameListModel = target("/users")
                .queryParam("q", user2.getUsername().substring(8, 14))
                .queryParam("maxResults", "10")
                .request()
                .header("Cookie", cookie)
                .accept(MediaType.APPLICATION_JSON)
                .get(UserListModel.class);

        List<UserModel> users = gameListModel.getList();
        Assert.assertEquals(1, users.size());
        Assert.assertEquals(user2, users.get(0));

    }

    @Test
    public void testSearchUsersByPrefix() {
        String cookie = login(fooUser.getUsername(), "foo");
        LOG.info("Cookie={}", cookie);

        UserListModel gameListModel = target("/users")
                .queryParam("q", user1.getUsername())
                .queryParam("maxResults", "10")
                .request()
                .header("Cookie", cookie)
                .accept(MediaType.APPLICATION_JSON)
                .get(UserListModel.class);

        List<UserModel> users = gameListModel.getList();
        Assert.assertEquals(2, users.size());
        Assert.assertEquals(user1, users.get(0));
        Assert.assertEquals(user2, users.get(1));

    }
}
