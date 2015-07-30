package net.capps.word.rest.providers;

import net.capps.word.db.dao.GamesDAO;
import net.capps.word.game.ai.ScryTileAI;
import net.capps.word.game.board.Game;
import net.capps.word.game.move.Move;
import net.capps.word.game.move.MoveType;
import net.capps.word.game.tile.RackTile;
import net.capps.word.rest.models.ErrorModel;
import net.capps.word.rest.models.GameModel;
import net.capps.word.rest.models.MoveModel;
import net.capps.word.rest.models.UserModel;
import net.capps.word.util.ErrorOrResult;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Created by charlescapps on 6/24/15.
 */
public class SpecialActionsProvider {
    private static final SpecialActionsProvider INSTANCE = new SpecialActionsProvider();
    private static final ScryTileAI SCRY_TILE_AI = ScryTileAI.getInstance();

    private static final ErrorModel ERR_MISSING_GAME_ID = new ErrorModel("Missing query param 'gameId'");
    private static final ErrorModel ERR_INVALID_GAME_ID = new ErrorModel("gameId is invalid.");
    private static final ErrorModel ERR_INVALID_GAME_STATE = new ErrorModel("The game isn't in progress.");
    private static final ErrorModel ERR_INVALID_USER = new ErrorModel("It isn't your turn.");
    private static final ErrorModel ERR_NO_SCRY_TILE = new ErrorModel("The current player doesn't have any oracle tiles!");

    private static final ErrorModel ERR_NO_PLAY_WORD_MOVE_FOUND = new ErrorModel("No words found! Try grabbing tiles from the board or passing.");

    private static final GamesDAO gamesDAO = GamesDAO.getInstance();

    public static SpecialActionsProvider getInstance() {
        return INSTANCE;
    }

    public ErrorOrResult<GameModel> validateScryAction(Integer gameId, UserModel authUser, Connection dbConn)
            throws SQLException {
        final int USER_ID = authUser.getId();

        if (gameId == null) {
            return ErrorOrResult.ofError(ERR_MISSING_GAME_ID);
        }

        Optional<GameModel> gameModelOpt = gamesDAO.getGameById(gameId, dbConn);

        if (!gameModelOpt.isPresent()) {
            return ErrorOrResult.ofError(ERR_INVALID_GAME_ID);
        }

        GameModel gameModel = gameModelOpt.get();
        final int CURR_PLAYER_ID = gameModel.getCurrentPlayerId();

        // Validate the game is in progress or offered
        // and it's the authenticated user's turn.
        switch (gameModel.getGameResult()) {
            case OFFERED:
                if (!gameModel.getPlayer1Turn() || USER_ID != CURR_PLAYER_ID) {
                    return ErrorOrResult.ofError(ERR_INVALID_USER);
                }
                break;
            case IN_PROGRESS:
                if (USER_ID != CURR_PLAYER_ID) {
                    return ErrorOrResult.ofError(ERR_INVALID_USER);
                }
                break;
            default:
                return ErrorOrResult.ofError(ERR_INVALID_GAME_STATE);
        }

        String rack = gameModel.getCurrentPlayerRack();
        if (!rack.contains(Character.toString(RackTile.SCRY_RACK_TILE))) {
            return ErrorOrResult.ofError(ERR_NO_SCRY_TILE);
        }

        return ErrorOrResult.ofResult(gameModel);
    }

    public ErrorOrResult<MoveModel> getScryMoveAndUpdateUserRack(GameModel validatedGame, UserModel authUser, Connection dbConn)
            throws Exception {
        Game game = new Game(validatedGame, Optional.empty());

        final String currentRack = validatedGame.getCurrentPlayerRack();
        final String updatedRack = currentRack.replaceFirst("[\\^]", "");
        if (updatedRack.length() != currentRack.length() - 1) {
            throw new IllegalStateException("Player's rack didn't contain '^' -- a Scry Tile");
        }

        Move scryMove = SCRY_TILE_AI.getNextMove(game);
        if (scryMove.getMoveType() != MoveType.PLAY_WORD) {
            return ErrorOrResult.ofError(ERR_NO_PLAY_WORD_MOVE_FOUND);
        }

        if (authUser.getId().equals(validatedGame.getPlayer1())) {
            gamesDAO.updatePlayer1Rack(validatedGame.getId(), updatedRack, dbConn);
        } else if (authUser.getId().equals(validatedGame.getPlayer2())) {
            gamesDAO.updatePlayer2Rack(validatedGame.getId(), updatedRack, dbConn);
        } else {
            throw new IllegalStateException();
        }

        MoveModel moveModel = scryMove.toMoveModel(authUser.getId(), 0);
        return ErrorOrResult.ofResult(moveModel);
    }


}
