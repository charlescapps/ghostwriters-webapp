package net.capps.word.rest.providers;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import net.capps.word.db.dao.GamesDAO;
import net.capps.word.db.dao.MovesDAO;
import net.capps.word.game.ai.GameAI;
import net.capps.word.game.board.GameState;
import net.capps.word.game.common.AiType;
import net.capps.word.game.common.GameResult;
import net.capps.word.game.move.Move;
import net.capps.word.game.move.MoveType;
import net.capps.word.game.tile.RackTile;
import net.capps.word.rest.models.ErrorModel;
import net.capps.word.rest.models.GameModel;
import net.capps.word.rest.models.MoveModel;
import net.capps.word.rest.models.UserModel;
import net.capps.word.util.ErrorOrResult;

import java.sql.Connection;
import java.util.List;

import static java.lang.String.format;

/**
 * Created by charlescapps on 1/24/15.
 */
public class MovesProvider {
    private static final MovesProvider INSTANCE = new MovesProvider();
    private static final GamesDAO gamesDAO = GamesDAO.getInstance();
    private static final MovesDAO movesDAO = MovesDAO.getInstance();

    public static MovesProvider getInstance() {
        return INSTANCE;
    }

    private MovesProvider() { } // Singleton pattern

    public ErrorOrResult<GameModel> validateMove(MoveModel inputMoveModel, UserModel authUser) throws Exception {

        Optional<ErrorModel> errorOpt = validateFieldsArePresent(inputMoveModel);
        if (errorOpt.isPresent()) {
            return ErrorOrResult.ofError(errorOpt.get());
        }

        errorOpt = isValidLettersField(inputMoveModel.getLetters());
        if (errorOpt.isPresent()) {
            return ErrorOrResult.ofError(errorOpt.get());
        }

        Integer gameId = inputMoveModel.getGameId();

        // Get the full Game using the gameId
        Optional<GameModel> gameOpt = gamesDAO.getGameWithPlayerModelsById(gameId);
        if (!gameOpt.isPresent()) {
            return ErrorOrResult.ofError(new ErrorModel(format("gameId %d is not a valid game.", gameId)));
        }
        GameModel game = gameOpt.get();

        // Verify that the authenticated user is the player whose turn it is for this game.
        boolean isPlayer1Turn = game.getPlayer1Turn();
        if (isPlayer1Turn) {
            if (!authUser.getId().equals(game.getPlayer1())) {
                return ErrorOrResult.ofError(new ErrorModel("It's not your turn!"));
            }
        } else {
            if (!authUser.getId().equals(game.getPlayer2())) {
                return ErrorOrResult.ofError(new ErrorModel("It's not your turn!"));
            }
        }

        // Create a Board object
        GameState gameState = new GameState(game, Optional.<Move>absent());
        Move move = new Move(inputMoveModel);

        // Check if it's a valid move.
        Optional<String> moveErrorOpt = gameState.getMoveError(move);
        if (moveErrorOpt.isPresent()) {
            return ErrorOrResult.ofError(new ErrorModel(moveErrorOpt.get()));
        }

        // Set the playerId as the currently authenticated user.
        inputMoveModel.setPlayerId(authUser.getId());

        return ErrorOrResult.ofResult(game);
    }

    public GameModel playMove(MoveModel validatedMove, GameModel gameModel, Connection dbConn) throws Exception {
        Preconditions.checkArgument(validatedMove.getGameId() != null &&
                                    validatedMove.getGameId().equals(gameModel.getId()),
                "The game ID on the move must match the original game's ID");

        List<MoveModel> prevMove = MovesDAO.getInstance().getMostRecentMoves(validatedMove.getGameId(), 1, dbConn);
        Optional<Move> previousMoveOpt = prevMove.size() == 1 ?
                Optional.of(new Move(prevMove.get(0))) :
                Optional.<Move>absent();

        GameState gameState = new GameState(gameModel, previousMoveOpt);
        Move move = new Move(validatedMove);

        int numPoints = gameState.playMove(move); // Play the move, updating the game state.

        GameModel updatedGame = gamesDAO.updateGame(gameState, validatedMove, numPoints, dbConn);
        updatedGame.setPlayer1Model(gameModel.getPlayer1Model());
        updatedGame.setPlayer2Model(gameModel.getPlayer2Model());

        return updatedGame;
    }

    public GameModel playAIMove(AiType aiType, GameModel gameModel, MoveModel previousMove, Connection dbConn) throws Exception {
        boolean isPlayer1Turn = gameModel.getPlayer1Turn();
        List<MoveModel> aiMoves = Lists.newArrayList();
        // While the turn hasn't changed and the game is still in progress, continue playing AI moves.
        while (gameModel.getPlayer1Turn() == isPlayer1Turn && gameModel.getGameResult() == GameResult.IN_PROGRESS) {
            gameModel = playeOneAIMove(aiType, gameModel, previousMove, aiMoves, dbConn);
        }

        gameModel.setLastMoves(aiMoves);

        return gameModel;
    }

    public void populateLastMoves(GameModel newGame, GameModel originalGame, MoveModel playedMove, Connection dbConn) throws Exception {
        // If the turn didn't change, then the lastMoves is an empty list
        if (originalGame.getPlayer1Turn() == newGame.getPlayer1Turn()) {
            newGame.setLastMoves(Lists.<MoveModel>newArrayList());
            return;
        }
        // Otherwise, query the database for the previous consecutive moves by the current player
        int playerId = playedMove.getPlayerId();
        List<MoveModel> lastMoves = movesDAO.getLastMovesByPlayer(playerId, newGame.getId(), dbConn);
        newGame.setLastMoves(lastMoves);
    }

    private GameModel playeOneAIMove(AiType aiType, GameModel gameModel, MoveModel lastHumanMove, List<MoveModel> aiMoves, Connection dbConn) throws Exception {

        GameAI gameAI = aiType.getGameAiInstance();
        int gameAiId = gameModel.getPlayer1Turn() ? gameModel.getPlayer1() : gameModel.getPlayer2();
        MoveModel lastMove = aiMoves.isEmpty() ? lastHumanMove : aiMoves.get(aiMoves.size() - 1);
        GameState gameState = new GameState(gameModel, Optional.of(new Move(lastMove)));
        Move aiMove = gameAI.getNextMove(gameState);

        int numPoints = gameState.playMove(aiMove);

        MoveModel aiMoveModel = aiMove.toMoveModel(gameAiId, numPoints);

        GameModel updatedGame = gamesDAO.updateGame(gameState, aiMoveModel, numPoints, dbConn);
        updatedGame.setPlayer1Model(gameModel.getPlayer1Model());
        updatedGame.setPlayer2Model(gameModel.getPlayer2Model());

        aiMoves.add(aiMoveModel);

        return updatedGame;

    }

    private Optional<ErrorModel> validateFieldsArePresent(MoveModel inputMoveModel) {
        if (inputMoveModel == null) {
            return Optional.of(new ErrorModel("Must provide a request body with an input move."));
        }
        if (inputMoveModel.getGameId() == null) {
            return Optional.of(new ErrorModel("Must provide \"gameId\" field"));
        }
        if (inputMoveModel.getMoveType() == null) {
            return Optional.of(new ErrorModel("Must provide valid \"moveType\" field."));
        }
        if (inputMoveModel.getMoveType() != MoveType.PASS) {
            if (inputMoveModel.getDir() == null) {
                return Optional.of(new ErrorModel("Must provide \"dir\" field"));
            }
            if (inputMoveModel.getStart() == null) {
                return Optional.of(new ErrorModel("Must provide \"start\" field"));
            }
            if (Strings.isNullOrEmpty(inputMoveModel.getLetters())) {
                return Optional.of(new ErrorModel("Must provide non-empty \"letters\" field"));
            }
            if (inputMoveModel.getTiles() == null || inputMoveModel.getTiles().isEmpty()) {
                return Optional.of(new ErrorModel("Must provide non-empty list of tiles that were played or grabbed."));
            }
        }
        return Optional.absent();
    }

    private Optional<ErrorModel> isValidLettersField(String letters) {
        for (int i = 0; i < letters.length(); i++) {
            char c = letters.charAt(i);
            if (!RackTile.isValidRackTile(c)) {
                return Optional.of(new ErrorModel("Each letter for a move must be an uppercase letter or wild card (*)"));
            }
        }
        return Optional.absent();
    }
}
