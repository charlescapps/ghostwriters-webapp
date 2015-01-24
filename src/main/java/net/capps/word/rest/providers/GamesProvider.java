package net.capps.word.rest.providers;

import com.google.common.base.Optional;
import net.capps.word.db.dao.GamesDAO;
import net.capps.word.db.dao.UsersDAO;
import net.capps.word.game.board.FixedLayouts;
import net.capps.word.game.board.SquareSet;
import net.capps.word.game.board.TileSet;
import net.capps.word.game.common.BoardSize;
import net.capps.word.game.common.BonusesType;
import net.capps.word.game.common.GameDensity;
import net.capps.word.game.gen.DefaultGameGenerator;
import net.capps.word.game.gen.DefaultLayoutGenerator;
import net.capps.word.game.gen.GameGenerator;
import net.capps.word.game.gen.LayoutGenerator;
import net.capps.word.rest.models.ErrorModel;
import net.capps.word.rest.models.GameModel;
import net.capps.word.rest.models.UserModel;
import net.capps.word.rest.services.GamesService;

import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static java.lang.String.format;

/**
 * Created by charlescapps on 1/18/15.
 */
public class GamesProvider {
    // -------------- Static -------------
    private static final GamesProvider INSTANCE = new GamesProvider();
    private static final GameGenerator gameGenerator = new DefaultGameGenerator();
    private static final LayoutGenerator layoutGenerator = new DefaultLayoutGenerator();

    // -------------- Private fields ---------

    public static GamesProvider getInstance() {
        return INSTANCE;
    }

    // --------------- Constructors --------
    private GamesProvider() { }

    // --------------- Public --------------
    public Optional<ErrorModel> validateInputForCreateGame(GameModel input, UserModel authUser) throws Exception {
        if (input.getId() != null) {
            return Optional.of(new ErrorModel("The game id should not be specified prior to creation."));
        }
        if (input.getPlayer1() == null || input.getPlayer2() == null) {
            return Optional.of(new ErrorModel("Missing id1 or id2 field!"));
        }
        if (input.getPlayer1().equals(input.getPlayer2())) {
            return Optional.of(new ErrorModel("Cannot start a game with yourself!"));
        }
        if (input.getBoardSize() == null) {
            return Optional.of(new ErrorModel("Missing boardSize field!"));
        }
        if (input.getBonusesType() == null) {
            return Optional.of(new ErrorModel("Missing bounesType field!"));
        }
        if (input.getGameDensity() == null) {
            return Optional.of(new ErrorModel("Missing gameDensity field!"));
        }
        if (!input.getPlayer1().equals(authUser.getId())) {
            return Optional.of(new ErrorModel("Player 1 must be the currently authenticated user."));
        }
        Optional<UserModel> player2 = UsersDAO.getInstance().getUserById(input.getPlayer2());
        if (!player2.isPresent()) {
            return Optional.of(new ErrorModel(format("Player 2 id %d isn't a valid User id.", input.getPlayer2())));
        }
        return Optional.absent();
    }

    public URI getGameURI(int gameId, UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(GamesService.GAMES_PATH)
                .path(Integer.toString(gameId))
                .build();
    }

    public GameModel createNewGame(GameModel validatedInputGame) throws Exception {
        BoardSize bs = validatedInputGame.getBoardSize();
        GameDensity gd = validatedInputGame.getGameDensity();
        int numWords = gd.getNumWords(bs);

        TileSet tileSet = gameGenerator.generateRandomFinishedGame(bs.getN(), numWords, bs.getMaxInitialWordSize());

        BonusesType bt = validatedInputGame.getBonusesType();

        SquareSet squareSet = bt == BonusesType.FIXED_BONUSES ?
                FixedLayouts.getInstance().getFixedLayout(bs) :
                layoutGenerator.generateRandomBonusLayout(bs);

        return GamesDAO.getInstance().createNewGame(validatedInputGame, tileSet, squareSet);

    }



    // --------------- Private --------------

}
