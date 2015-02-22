package net.capps.word.rest.providers;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import net.capps.word.db.dao.GamesDAO;
import net.capps.word.game.board.GameState;
import net.capps.word.game.move.Move;
import net.capps.word.rest.models.ErrorModel;
import net.capps.word.rest.models.GameModel;
import net.capps.word.rest.models.MoveModel;
import net.capps.word.rest.models.UserModel;

import static java.lang.String.format;

/**
 * Created by charlescapps on 1/24/15.
 */
public class MovesProvider {
    private static final MovesProvider INSTANCE = new MovesProvider();

    public static MovesProvider getInstance() {
        return INSTANCE;
    }

    private MovesProvider() { } // Singleton pattern

    public Optional<ErrorModel> validateMove(MoveModel inputMoveModel, UserModel authUser) throws Exception {

        Optional<ErrorModel> errorOpt = validateFieldsArePresent(inputMoveModel);
        if (errorOpt.isPresent()) {
            return errorOpt;
        }

        Integer gameId = inputMoveModel.getGameId();

        // Get the full Game using the gameId
        Optional<GameModel> gameOpt = GamesDAO.getInstance().getGameById(gameId);
        if (!gameOpt.isPresent()) {
            return Optional.of(new ErrorModel(format("gameId %d is not a valid game.", gameId)));
        }
        GameModel game = gameOpt.get();

        // Verify that the authenticated user is the player whose turn it is for this game.
        boolean isPlayer1Turn = game.getPlayer1Turn();
        if (isPlayer1Turn) {
            if (!authUser.getId().equals(game.getPlayer1())) {
                return Optional.of(new ErrorModel("It's not your turn!"));
            }
        } else {
            if (!authUser.getId().equals(game.getPlayer2())) {
                return Optional.of(new ErrorModel("It's not your turn!"));
            }
        }

        // Create a Board object
        GameState gameState = new GameState(game);
        Move move = new Move(inputMoveModel);

        // Check if it's a valid move.
        Optional<String> moveErrorOpt = gameState.isValidMove(move);
        if (moveErrorOpt.isPresent()) {
            return Optional.of(new ErrorModel(moveErrorOpt.get()));
        }

        return Optional.absent();
    }

    public GameModel playMove(MoveModel validatedMove) throws Exception {
        // Get the full Game using the gameId
        int gameId = validatedMove.getGameId();

        Optional<GameModel> gameOpt = GamesDAO.getInstance().getGameById(gameId);
        if (!gameOpt.isPresent()) {
            throw new IllegalStateException(format("gameId %d is not a valid game.", gameId));
        }
        GameModel gameModel = gameOpt.get();
        GameState gameState = new GameState(gameModel);
        Move move = new Move(validatedMove);

        int numPoints = gameState.playMove(move); // Play the move, updating the game state.

        GameModel updatedGame = GamesDAO.getInstance().updateGame(gameState, validatedMove, numPoints);
        updatedGame.setLastMove(validatedMove);
        return updatedGame;
    }

    private Optional<ErrorModel> validateFieldsArePresent(MoveModel inputMoveModel) {
        if (inputMoveModel == null) {
            return Optional.of(new ErrorModel("Must provide a request body with an input move."));
        }
        if (inputMoveModel.getGameId() == null) {
            return Optional.of(new ErrorModel("Must provide \"gameId\" field"));
        }
        if (inputMoveModel.getDir() == null) {
            return Optional.of(new ErrorModel("Must provide \"dir\" field"));
        }
        if (inputMoveModel.getStart() == null) {
            return Optional.of(new ErrorModel("Must provide \"start\" field"));
        }
        if (Strings.isNullOrEmpty(inputMoveModel.getLetters())) {
            return Optional.of(new ErrorModel("Must provide non-empty \"letters\" field"));
        }
        if (inputMoveModel.getMoveType() == null) {
            return Optional.of(new ErrorModel("Must provide \"moveType\" field"));
        }
        if (inputMoveModel.getTiles() == null || inputMoveModel.getTiles().isEmpty()) {
            return Optional.of(new ErrorModel("Must provide non-empty list of tiles that were played or grabbed."));
        }
        return Optional.absent();
    }
}
