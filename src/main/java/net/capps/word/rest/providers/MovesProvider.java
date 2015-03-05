package net.capps.word.rest.providers;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import net.capps.word.db.dao.GamesDAO;
import net.capps.word.db.dao.MovesDAO;
import net.capps.word.db.dao.UsersDAO;
import net.capps.word.game.ai.GameAi;
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

import java.sql.Connection;
import java.util.List;

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

        errorOpt = isValidLettersField(inputMoveModel.getLetters());
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
        GameState gameState = new GameState(game, Optional.<Move>absent());
        Move move = new Move(inputMoveModel);

        // Check if it's a valid move.
        Optional<String> moveErrorOpt = gameState.getMoveError(move);
        if (moveErrorOpt.isPresent()) {
            return Optional.of(new ErrorModel(moveErrorOpt.get()));
        }

        return Optional.absent();
    }

    public GameModel playMove(MoveModel validatedMove, Connection dbConn) throws Exception {
        // Get the full Game using the gameId
        int gameId = validatedMove.getGameId();

        Optional<GameModel> gameOpt = GamesDAO.getInstance().getGameById(gameId, dbConn);
        if (!gameOpt.isPresent()) {
            throw new IllegalStateException(format("gameId %d is not a valid game.", gameId));
        }

        List<MoveModel> prevMove = MovesDAO.getInstance().getMostRecentMoves(gameId, 1, dbConn);
        Optional<Move> previousMoveOpt = prevMove.size() == 1 ?
                Optional.of(new Move(prevMove.get(0))) :
                Optional.<Move>absent();

        GameModel gameModel = gameOpt.get();
        GameState gameState = new GameState(gameModel, previousMoveOpt);
        Move move = new Move(validatedMove);

        int numPoints = gameState.playMove(move); // Play the move, updating the game state.

        GameModel updatedGame = GamesDAO.getInstance().updateGame(gameState, validatedMove, numPoints, dbConn);
        updatedGame.setLastMove(validatedMove);
        Optional<UserModel> player1Model = UsersDAO.getInstance().getUserById(updatedGame.getPlayer1());
        Optional<UserModel> player2Model = UsersDAO.getInstance().getUserById(updatedGame.getPlayer2());

        if (!player1Model.isPresent()) {
            throw new IllegalStateException(String.format("Player 1 with ID %d wasn't present in the database", updatedGame.getPlayer1()));
        }

        if (!player2Model.isPresent()) {
            throw new IllegalStateException(String.format("Player 2 with ID %d wasn't present in the database", updatedGame.getPlayer2()));
        }

        updatedGame.setPlayer1Model(player1Model.get());
        updatedGame.setPlayer2Model(player2Model.get());

        return updatedGame;
    }

    public GameModel playAIMove(AiType aiType, GameModel gameModel, MoveModel previousMove, Connection dbConn) throws Exception {
        if (gameModel.getGameResult() != GameResult.IN_PROGRESS) {
            return gameModel;
        }

        GameAi gameAi = aiType.getGameAiInstance();
        GameState gameState = new GameState(gameModel, Optional.of(new Move(previousMove)));
        Move aiMove = gameAi.getNextMove(gameState);

        int numPoints = gameState.playMove(aiMove);

        MoveModel aiMoveModel = aiMove.toMoveModel(numPoints);

        GameModel updatedGame = GamesDAO.getInstance().updateGame(gameState, aiMoveModel, numPoints, dbConn);

        updatedGame.setLastMove(aiMoveModel);
        Optional<UserModel> player1Model = UsersDAO.getInstance().getUserById(updatedGame.getPlayer1());
        Optional<UserModel> player2Model = UsersDAO.getInstance().getUserById(updatedGame.getPlayer2());

        if (!player1Model.isPresent()) {
            throw new IllegalStateException(String.format("Player 1 with ID %d wasn't present in the database", updatedGame.getPlayer1()));
        }

        if (!player2Model.isPresent()) {
            throw new IllegalStateException(String.format("Player 2 with ID %d wasn't present in the database", updatedGame.getPlayer2()));
        }

        updatedGame.setPlayer1Model(player1Model.get());
        updatedGame.setPlayer2Model(player2Model.get());

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
