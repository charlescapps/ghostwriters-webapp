package net.capps.word.rest.providers;

import com.google.common.base.Optional;
import net.capps.word.db.dao.GamesDAO;
import net.capps.word.game.board.GameState;
import net.capps.word.game.common.BoardSize;
import net.capps.word.game.move.Move;
import net.capps.word.rest.models.MoveModel;
import net.capps.word.rest.models.ErrorModel;
import net.capps.word.rest.models.GameModel;
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
        if (inputMoveModel == null) {
            return Optional.of(new ErrorModel("Must provide a request body with an input move."));
        }
        Integer gameId = inputMoveModel.getGameId();
        if (gameId == null) {
            return Optional.of(new ErrorModel("Must provide a gameId"));
        }

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
                return Optional.of(new ErrorModel(
                        format("Authenticated user (%d) is not the current player (%d) for this game.", authUser.getId(), game.getPlayer1())));
            }
        } else {
            if (!authUser.getId().equals(game.getPlayer2())) {
                return Optional.of(new ErrorModel(
                        format("Authenticated user (%d) is not the current player (%d) for this game.", authUser.getId(), game.getPlayer2())));
            }
        }

        // Validate all Move fields.
        if (inputMoveModel.getDir() == null || inputMoveModel.getStart() == null || inputMoveModel.getLetters() == null || inputMoveModel.getMoveType() == null) {
            return Optional.of(new ErrorModel("Must provide 'dir', 'start', 'letters', and 'moveType' fields"));
        }
        if (inputMoveModel.getTiles() == null || inputMoveModel.getTiles().isEmpty()) {
            return Optional.of(new ErrorModel("Must provide non-empty list of tiles that were played or grabbed."));
        }

        // Create a Board object
        BoardSize bs = game.getBoardSize();
        GameState gameState = new GameState(game);
        Move move = new Move(inputMoveModel);

        // Check if it's a valid move.
        Optional<String> moveErrorOpt = gameState.isValidMove(move);
        if (moveErrorOpt.isPresent()) {
            return Optional.of(new ErrorModel(moveErrorOpt.get()));
        }

        // If so, play the move.


        
    }
}
