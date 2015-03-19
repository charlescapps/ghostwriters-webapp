package net.capps.word.rest.providers;

import com.google.common.base.Optional;
import net.capps.word.db.dao.GamesDAO;
import net.capps.word.rest.models.ErrorModel;
import net.capps.word.rest.models.GameModel;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by charlescapps on 3/18/15.
 */
public class GamesSearchProvider {
    private static final GamesSearchProvider INSTANCE = new GamesSearchProvider();
    private static final GamesDAO gamesDAO = GamesDAO.getInstance();

    private static final int MAX_COUNT = 50;

    public static GamesSearchProvider getInstance() {
        return INSTANCE;
    }

    public Optional<ErrorModel> validateSearchParams(Integer userId, Integer count, Boolean inProgress) {
        if (userId == null || userId <= 0) {
            return Optional.of(new ErrorModel("Must specify 'userId' query param, and it must be > 0"));
        }
        if (count == null) {
            return Optional.of(new ErrorModel("Must specifiy the 'count' query param, of type int"));
        }
        if (count <= 0 || count > MAX_COUNT) {
            return Optional.of(new ErrorModel("The 'count' query param must be > 0 and <= " + MAX_COUNT));
        }
        if (inProgress == null) {
            return Optional.of(new ErrorModel("Must specify the 'inProgress' query param, of type boolean."));
        }
        return Optional.absent();
    }

    public List<GameModel> getGamesForUser(int userId, int count, boolean inProgress) throws SQLException {
        if (inProgress) {
            return gamesDAO.getInProgressGamesForUserDateStartedDesc(userId, count);
        } else {
            return gamesDAO.getFinishedGamesForUserDateStartedDesc(userId, count);
        }
    }
}
