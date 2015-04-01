package net.capps.word.rest.providers;

import com.google.common.base.Optional;
import net.capps.word.constants.WordConstants;
import net.capps.word.db.dao.GamesDAO;
import net.capps.word.db.dao.UsersDAO;
import net.capps.word.game.board.FixedLayouts;
import net.capps.word.game.board.SquareSet;
import net.capps.word.game.board.TileSet;
import net.capps.word.game.common.*;
import net.capps.word.game.gen.DefaultGameGenerator;
import net.capps.word.game.gen.DefaultSquareSetGenerator;
import net.capps.word.game.gen.GameGenerator;
import net.capps.word.game.gen.SquareSetGenerator;
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
    private static final SquareSetGenerator SQUARE_SET_GENERATOR = new DefaultSquareSetGenerator();

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
        if (input.getGameType() == null) {
            return Optional.of(new ErrorModel("Missing gameType field. Must be \"SINGLE_PLAYER\" or \"TWO_PLAYER\""));
        }
        if (!authUser.getId().equals(input.getPlayer1())) {
            return Optional.of(new ErrorModel("Player 1 must be the currently authenticated user."));
        }

        // Verify two player fields
        if (input.getGameType() == GameType.TWO_PLAYER) {
            if (input.getPlayer2() == null) {
                return Optional.of(new ErrorModel("Missing player2 field for multi-player game!"));
            }
            Optional<UserModel> player2 = UsersDAO.getInstance().getUserById(input.getPlayer2());
            if (!player2.isPresent()) {
                return Optional.of(new ErrorModel(format("Player 2 id %d isn't a valid User id.", input.getPlayer2())));
            }

            checkIfGameIsAgainstAI(input, authUser);
        }
        // Verify single player fields
        else if (input.getGameType() == GameType.SINGLE_PLAYER && input.getAiType() == null) {
            return Optional.of(new ErrorModel("Missing aiType field for single player type game."));
        }

        if (input.getPlayer1().equals(input.getPlayer2())) {
            return Optional.of(new ErrorModel("Cannot start a game with yourself!"));
        }
        if (input.getBoardSize() == null) {
            return Optional.of(new ErrorModel("Missing boardSize field!"));
        }
        if (input.getBonusesType() == null) {
            return Optional.of(new ErrorModel("Missing bonusesType field!"));
        }
        if (input.getGameDensity() == null) {
            return Optional.of(new ErrorModel("Missing gameDensity field!"));
        }

        return Optional.absent();
    }

    public URI getGameURI(int gameId, UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(GamesService.GAMES_PATH)
                .path(Integer.toString(gameId))
                .build();
    }

    public GameModel createNewGame(GameModel validatedInputGame, UserModel gameCreator) throws Exception {
        BoardSize bs = validatedInputGame.getBoardSize();
        GameDensity gd = validatedInputGame.getGameDensity();
        int numWords = gd.getNumWords(bs);

        TileSet tileSet = gameGenerator.generateRandomFinishedGame(bs.getN(), numWords, bs.getMaxInitialWordSize());

        BonusesType bt = validatedInputGame.getBonusesType();

        SquareSet squareSet = bt == BonusesType.FIXED_BONUSES ?
                FixedLayouts.getInstance().getFixedLayout(bs) :
                SQUARE_SET_GENERATOR.generateRandomBonusLayout(bs);

        // Add the system AI user for single player games
        if (validatedInputGame.getGameType() == GameType.SINGLE_PLAYER) {
            String systemUsername = validatedInputGame.getAiType().getSystemUsername();
            Optional<UserModel> systemUser = UsersDAO.getInstance().getUserByUsername(systemUsername, true);
            if (!systemUser.isPresent()) {
                throw new IllegalStateException("AI users not in the database!");
            }
            validatedInputGame.setPlayer2(systemUser.get().getId());
        }

        GameModel createdGame = GamesDAO.getInstance().createNewGame(validatedInputGame, tileSet, squareSet);
        Optional<UserModel> player2Model = UsersDAO.getInstance().getUserById(createdGame.getPlayer2());
        if (!player2Model.isPresent()) {
            throw new Exception("Error - player2 was not found in the database. User ID is: " + createdGame.getPlayer2());
        }

        createdGame.setPlayer1Model(gameCreator);
        createdGame.setPlayer2Model(player2Model.get());

        return createdGame;
    }



    // --------------- Private --------------

    /**
     * This is a bit of a kludge.
     * Want the "best match" button to possibly match you up against an AI, especially early on
     * when there aren't many players.
     *
     * So...we need to detect if the best match endpoint returned one of the AI users.
     * If so, then convert the game to a SINGLE_PLAYER game.
     */
    private void checkIfGameIsAgainstAI(GameModel gameModel, UserModel authUser) {
        int opponentId = gameModel.getPlayer1().equals(authUser.getId()) ?
                gameModel.getPlayer2() :
                gameModel.getPlayer1();

        if (opponentId == WordConstants.RANDOM_AI_USER.get().getId()) {
            gameModel.setAiType(AiType.RANDOM_AI);
            gameModel.setGameType(GameType.SINGLE_PLAYER);
        } else if (opponentId == WordConstants.BOOKWORM_AI_USER.get().getId()) {
            gameModel.setAiType(AiType.BOOKWORM_AI);
            gameModel.setGameType(GameType.SINGLE_PLAYER);
        } else if (opponentId == WordConstants.PROFESSOR_AI_USER.get().getId()) {
            gameModel.setAiType(AiType.PROFESSOR_AI);
            gameModel.setGameType(GameType.SINGLE_PLAYER);
        }
    }
}
