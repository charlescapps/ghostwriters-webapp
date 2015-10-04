package net.capps.word.rest.providers;

import com.google.common.base.Strings;
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
import net.capps.word.game.tile.RackTile;
import net.capps.word.rest.models.ErrorModel;
import net.capps.word.rest.models.GameModel;
import net.capps.word.rest.models.UserModel;
import net.capps.word.rest.services.GamesService;
import net.capps.word.util.ErrorOrResult;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Created by charlescapps on 1/18/15.
 */
public class GamesProvider {
    // -------------- Static -------------
    private static final GamesProvider INSTANCE = new GamesProvider();
    private static final GameGenerator DEFAULT_GAME_GENERATOR = DefaultGameGenerator.getInstance();
    private static final SquareSetGenerator SQUARE_SET_GENERATOR = DefaultSquareSetGenerator.getInstance();
    private static final GamesDAO gamesDAO = GamesDAO.getInstance();
    private static final UsersDAO usersDAO = UsersDAO.getInstance();
    public static final Pattern INITIAL_RACK_PATTERN = Pattern.compile("\\*{0,4}\\^{0,4}");

    // -------------- Errors ------------
    private static final ErrorModel ERR_GAME_ID_PRESENT = new ErrorModel("The \"gameId\" field should not be specified.");
    private static final ErrorModel ERR_MISSING_GAME_TYPE_FIELD = new ErrorModel("Missing \"gameType\" field. Must be \"SINGLE_PLAYER\" or \"TWO_PLAYER\"");
    private static final ErrorModel ERR_PLAYER1_MUST_BE_AUTH_USER = new ErrorModel("Player 1 must be the logged in user.");
    private static final ErrorModel ERR_MISSING_PLAYER2_ID = new ErrorModel("Missing \"player2\" field for multi player game.");
    private static final ErrorModel ERR_INVALID_PLAYER2_ID = new ErrorModel("Player 2 id isn't a valid User id.");
    private static final ErrorModel ERR_INVALID_GAME_ID = new ErrorModel("Invalid game ID.");

    private static final ErrorModel ERR_MISSING_AI_TYPE = new ErrorModel("Missing \"aiType\" field for single player game.");
    private static final ErrorModel ERR_CANNOT_START_GAME_WITH_SELF = new ErrorModel("Cannot start a game with yourself!");

    private static final ErrorModel ERR_MISSING_BOARD_SIZE = new ErrorModel("Missing \"boardSize\" field.");
    private static final ErrorModel ERR_MISSING_BONUSES_TYPE = new ErrorModel("Missing \"bonusesType\" field.");
    private static final ErrorModel ERR_MISSING_GAME_DENSITY = new ErrorModel("Missing \"gameDensity\" field.");
    private static final ErrorModel ERR_INVALID_PLAYER1_RACK = new ErrorModel("Invalid \"player1Rack\" field. If present, can only contain 0-4 blank tiles ('*') and 0-2 scry tiles ('^')");
    private static final ErrorModel ERR_INVALID_RACK_PARAM = new ErrorModel("Invalid \"rack\" param. If present, can only contain 0-4 blank tiles ('*') and 0-2 scry tiles ('^')");

    private static final ErrorModel ERR_INVALID_ACCEPT_OR_REJECT_USER = new ErrorModel("You've already joined this game and can find it in My Games.");

    // -------------- Private fields ---------

    public static GamesProvider getInstance() {
        return INSTANCE;
    }

    // --------------- Constructors --------
    private GamesProvider() { }

    // --------------- Public --------------
    public Optional<ErrorModel> validateInputForCreateGame(Connection dbConn, GameModel input, UserModel authUser) throws Exception {
        if (input.getId() != null) {
            return Optional.of(ERR_GAME_ID_PRESENT);
        }
        if (input.getGameType() == null) {
            return Optional.of(ERR_MISSING_GAME_TYPE_FIELD);
        }
        if (!authUser.getId().equals(input.getPlayer1())) {
            return Optional.of(ERR_PLAYER1_MUST_BE_AUTH_USER);
        }

        // Verify two player fields
        if (input.getGameType() == GameType.TWO_PLAYER) {
            if (input.getPlayer2() == null) {
                return Optional.of(ERR_MISSING_PLAYER2_ID);
            }
            Optional<UserModel> player2 = usersDAO.getUserById(dbConn, input.getPlayer2());
            if (!player2.isPresent()) {
                return Optional.of(ERR_INVALID_PLAYER2_ID);
            }

            checkIfGameIsAgainstAI(input, authUser);
        }
        // Verify single player fields
        else if (input.getGameType() == GameType.SINGLE_PLAYER && input.getAiType() == null) {
            return Optional.of(ERR_MISSING_AI_TYPE);
        }

        if (input.getPlayer1().equals(input.getPlayer2())) {
            return Optional.of(ERR_CANNOT_START_GAME_WITH_SELF);
        }
        if (input.getBoardSize() == null) {
            return Optional.of(ERR_MISSING_BOARD_SIZE);
        }
        if (input.getBonusesType() == null) {
            return Optional.of(ERR_MISSING_BONUSES_TYPE);
        }
        if (input.getGameDensity() == null) {
            return Optional.of(ERR_MISSING_GAME_DENSITY);
        }
        if (input.getPlayer1Rack() != null) {
            String initialRack = input.getPlayer1Rack();
            if (!INITIAL_RACK_PATTERN.matcher(initialRack).matches()) {
                return Optional.of(ERR_INVALID_PLAYER1_RACK);
            }
        }

        return Optional.empty();
    }

    public ErrorOrResult<GameModel> validateAcceptGameOffer(int gameId, String rack, UserModel authUser, Connection dbConn) throws SQLException {
        Optional<GameModel> gameOpt = gamesDAO.getGameById(gameId, dbConn);
        if (!gameOpt.isPresent()) {
            return ErrorOrResult.ofError(ERR_INVALID_GAME_ID);
        }

        GameModel game = gameOpt.get();
        if (!game.getPlayer2().equals(authUser.getId()) || game.getGameResult() != GameResult.OFFERED) {
            return ErrorOrResult.ofError(ERR_INVALID_ACCEPT_OR_REJECT_USER);
        }

        if (rack != null && !INITIAL_RACK_PATTERN.matcher(rack).matches()) {
            return ErrorOrResult.ofError(ERR_INVALID_RACK_PARAM);
        }

        return ErrorOrResult.ofResult(game);
    }

    public ErrorOrResult<GameModel> validateRejectGameOffer(int gameId, UserModel authUser, Connection dbConn) throws SQLException {
        Optional<GameModel> gameOpt = gamesDAO.getGameById(gameId, dbConn);
        if (!gameOpt.isPresent()) {
            return ErrorOrResult.ofError(ERR_INVALID_GAME_ID);
        }

        GameModel game = gameOpt.get();
        if (!game.getPlayer2().equals(authUser.getId()) || game.getGameResult() != GameResult.OFFERED) {
            return ErrorOrResult.ofError(ERR_INVALID_ACCEPT_OR_REJECT_USER);
        }

        return ErrorOrResult.ofResult(game);
    }

    public void acceptGameOfferAndUpdateRack(GameModel gameModel, String rack, Connection dbConn)
            throws Exception {

        if (!Strings.isNullOrEmpty(rack)) {
            gamesDAO.updatePlayer2Rack(gameModel.getId(), rack, dbConn);
        }

        gamesDAO.acceptGame(gameModel.getId(), dbConn);
    }

    /**
     * The challenged player gets a free ? tile ('*')
     * @param rack
     */
    public String updateRackForChallengedPlayer(String rack) {
        if (rack == null) {
            rack = "";
        }
        return rack + RackTile.WILD_RACK_TILE;
    }

    public URI getGameURI(int gameId, UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(GamesService.GAMES_PATH)
                .path(Integer.toString(gameId))
                .build();
    }

    public GameModel createNewGame(GameModel validatedInputGame, UserModel gameCreator, Connection dbConn) throws Exception {
        BoardSize bs = validatedInputGame.getBoardSize();
        GameDensity gd = validatedInputGame.getGameDensity();
        int numWords = gd.getNumWords(bs);

        GameGenerator gameGenerator = getGameGenerator(validatedInputGame);
        TileSet tileSet = gameGenerator.generateRandomFinishedGame(bs.getN(), numWords, bs.getN());

        BonusesType bt = validatedInputGame.getBonusesType();

        SquareSet squareSet = bt == BonusesType.FIXED_BONUSES ?
                FixedLayouts.getInstance().getFixedLayout(bs) :
                SQUARE_SET_GENERATOR.generateRandomBonusLayout(bs);

        // Add the system AI user for single player games
        if (validatedInputGame.getGameType() == GameType.SINGLE_PLAYER) {
            String systemUsername = validatedInputGame.getAiType().getSystemUsername();
            Optional<UserModel> systemUser = usersDAO.getUserByUsername(dbConn, systemUsername, true);
            if (!systemUser.isPresent()) {
                throw new IllegalStateException("AI users not in the database!");
            }
            validatedInputGame.setPlayer2(systemUser.get().getId());
        }

        GameModel createdGame = gamesDAO.createNewGame(dbConn, validatedInputGame, tileSet, squareSet);
        Optional<UserModel> player2Model = usersDAO.getUserById(dbConn, createdGame.getPlayer2());
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

    private GameGenerator getGameGenerator(GameModel inputGame) {
        return inputGame.getSpecialDict() == null ?
                DEFAULT_GAME_GENERATOR :
                inputGame.getSpecialDict().getGameGenerator();

    }
}
