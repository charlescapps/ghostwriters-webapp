package net.capps.word.rest.providers;

import net.capps.word.db.dao.GamesDAO;
import net.capps.word.rest.models.ErrorModel;
import net.capps.word.rest.models.GameListModel;
import net.capps.word.rest.models.GameModel;
import net.capps.word.rest.models.UserModel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Created by charlescapps on 3/18/15.
 */
public class GamesSearchProvider {
    private static final GamesSearchProvider INSTANCE = new GamesSearchProvider();
    private static final GamesDAO gamesDAO = GamesDAO.getInstance();
    private static final MovesProvider movesProvider = MovesProvider.getInstance();

    private static final int MAX_COUNT = 100;

    // --------- Errors -----------
    private static final Optional<ErrorModel> ERR_MISSING_COUNT = Optional.of(new ErrorModel("Missing 'count' query param"));
    private static final Optional<ErrorModel> ERR_INVALID_COUNT = Optional.of(new ErrorModel("The 'count' query param must be > 0 and <= " + MAX_COUNT));
    private static final Optional<ErrorModel> ERR_INVALID_PAGE = Optional.of(new ErrorModel("The 'page' query param must be >= 0"));
    private static final Optional<ErrorModel> ERR_MISSING_IN_PROGRESS = Optional.of(new ErrorModel("Missing 'inProgress' query param"));

    public static GamesSearchProvider getInstance() {
        return INSTANCE;
    }

    public Optional<ErrorModel> validateSearchParams(int count, int page, Boolean inProgress) {
        if (count <= 0 || count > MAX_COUNT) {
            return ERR_INVALID_COUNT;
        }
        if (page < 0) {
            return ERR_INVALID_PAGE;
        }
        if (inProgress == null) {
            return ERR_MISSING_IN_PROGRESS;
        }
        return Optional.empty();
    }

    public Optional<ErrorModel> validateCount(int count) {
        if (count <= 0 || count > MAX_COUNT) {
            return ERR_MISSING_COUNT;
        }
        return Optional.empty();
    }

    public GameListModel getGamesForUserLastActivityDesc(UserModel authUser, int count, int page, boolean inProgress, boolean includeMoves, Connection dbConn) throws SQLException {
        List<GameModel> gameModels;

        if (inProgress) {
            gameModels = gamesDAO.getInProgressGamesForUserLastActivityDesc(authUser.getId(), count + 1, page * count, dbConn);
        } else {
            gameModels = gamesDAO.getFinishedGamesForUserLastActivityDesc(authUser.getId(), count + 1, page * count, dbConn);
        }
        if (includeMoves) {
            for (GameModel gameModel: gameModels) {
                movesProvider.populateLastMoves(gameModel, authUser, dbConn);
            }
        }

        // If we found (count + 1) results, then there is a next page
        if (gameModels.size() == count + 1) {
            return new GameListModel(gameModels.subList(0, count), page + 1);
        }
        // Otherwise, there is no next page, so set the "nextPage" field to null
        return new GameListModel(gameModels, null);

    }
}
