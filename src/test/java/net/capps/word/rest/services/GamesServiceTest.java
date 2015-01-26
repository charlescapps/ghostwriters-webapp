package net.capps.word.rest.services;

import net.capps.word.game.common.BoardSize;
import net.capps.word.game.common.BonusesType;
import net.capps.word.game.common.GameDensity;
import net.capps.word.rest.filters.InitialUserAuthFilter;
import net.capps.word.rest.filters.RegularUserAuthFilter;
import net.capps.word.rest.models.GameModel;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Assert;
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
    public void testCreateVentiGame() {
        doTestCreateGame(BoardSize.VENTI, BonusesType.RANDOM_BONUSES, GameDensity.REGULAR);
    }

    @Test
    public void testCreateAllGameTypes() {
        for (BoardSize boardSize: BoardSize.values()) {
            for (BonusesType bonusesType: BonusesType.values()) {
                for (GameDensity gameDensity: GameDensity.values()) {
                    doTestCreateGame(boardSize, bonusesType, gameDensity);
                }
            }
        }
    }

    private void doTestCreateGame(BoardSize boardSize, BonusesType bonusesType, GameDensity gameDensity) {
        String cookie = login(fooUser.getUsername(), "foo");
        LOG.info("Cookie={}", cookie);

        GameModel input = new GameModel();
        input.setBoardSize(boardSize);
        input.setBonusesType(bonusesType);
        input.setGameDensity(gameDensity);
        input.setPlayer1(fooUser.getId());
        input.setPlayer2(barUser.getId());

        GameModel result = target("games")
                .request()
                .header("Cookie", cookie)
                .build("POST", Entity.entity(input, MediaType.APPLICATION_JSON))
                .invoke(GameModel.class);

        LOG.info("Logging the game result:");
        LOG.info(result.toString());

        Assert.assertEquals(result.getPlayer1(), input.getPlayer1());
        Assert.assertEquals(result.getPlayer2(), input.getPlayer2());
        Assert.assertEquals("", result.getPlayer1Rack());
        Assert.assertEquals("", result.getPlayer2Rack());
        Assert.assertEquals(input.getBoardSize(), result.getBoardSize());
        Assert.assertEquals(input.getBonusesType(), result.getBonusesType());
        Assert.assertEquals(input.getGameDensity(), result.getGameDensity());
        Assert.assertEquals(new Integer(0), result.getPlayer1Points());
        Assert.assertEquals(new Integer(0), result.getPlayer2Points());
        final int N = input.getBoardSize().getN();
        Assert.assertEquals(N * N, result.getSquares().length());
        Assert.assertEquals(N * N, result.getTiles().length());
        Assert.assertTrue(result.getDateCreated() > 0);
    }




}
