package net.capps.word.rest.services;

import net.capps.word.game.common.BoardSize;
import net.capps.word.game.common.BonusesType;
import net.capps.word.game.common.GameDensity;
import net.capps.word.rest.filters.InitialUserAuthFilter;
import net.capps.word.rest.filters.RegularUserAuthFilter;
import net.capps.word.rest.models.GameModel;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

/**
 * Created by charlescapps on 1/24/15.
 */
public class GamesServiceTest extends BaseWordServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(GamesServiceTest.class);

    @Override
    protected Application configure() {
        return new ResourceConfig(LoginService.class, GamesService.class, MovesService.class,
                RegularUserAuthFilter.class, InitialUserAuthFilter.class);
    }

    @Test
    public void testCreateGame() {
        String cookie = login(fooUser.getUsername(), "foo");
        LOG.info("Cookie={}", cookie);

        GameModel input = new GameModel();
        input.setBoardSize(BoardSize.VENTI);
        input.setBonusesType(BonusesType.RANDOM_BONUSES);
        input.setGameDensity(GameDensity.REGULAR);
        input.setPlayer1(fooUser.getId());
        input.setPlayer2(barUser.getId());

        GameModel result = target("games")
                            .request()
                            .header("Cookie", cookie)
                            .build("POST", Entity.entity(input, MediaType.APPLICATION_JSON))
                            .invoke(GameModel.class);

        LOG.info(result.toString());


    }

    // ------------ Private ----------




}
