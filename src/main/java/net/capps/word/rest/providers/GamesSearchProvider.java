package net.capps.word.rest.providers;

import com.google.common.base.Optional;
import net.capps.word.db.dao.GamesDAO;
import net.capps.word.db.dao.UsersDAO;
import net.capps.word.rest.models.ErrorModel;
import net.capps.word.rest.models.GameModel;
import net.capps.word.rest.models.UserModel;

import java.sql.SQLException;
import java.util.List;

import static java.lang.String.format;

/**
 * Created by charlescapps on 3/18/15.
 */
public class GamesSearchProvider {
    private static final GamesSearchProvider INSTANCE = new GamesSearchProvider();
    private static final GamesDAO gamesDAO = GamesDAO.getInstance();
    private static final UsersDAO usersDAO = UsersDAO.getInstance();

    private static final int MAX_COUNT = 50;

    public static GamesSearchProvider getInstance() {
        return INSTANCE;
    }

    public Optional<ErrorModel> validateSearchParams(Integer count, Boolean inProgress) {
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

    public List<GameModel> getGamesForUser(UserModel authUser, int count, boolean inProgress) throws SQLException {
        List<GameModel> gameModels;
        if (inProgress) {
            gameModels = gamesDAO.getInProgressGamesForUserDateStartedDesc(authUser.getId(), count);
        } else {
            gameModels = gamesDAO.getFinishedGamesForUserDateStartedDesc(authUser.getId(), count);
        }
        for (GameModel gameModel: gameModels) {
            hydrateGameForAuthUserWithUserModels(gameModel, authUser);
        }
        return gameModels;
    }

    // ------------ Private -----------
    private void hydrateGameForAuthUserWithUserModels(GameModel gameModel, UserModel authUser) throws SQLException {
        if (gameModel.getPlayer1().equals(authUser.getId())) {
            gameModel.setPlayer1Model(authUser);
            Optional<UserModel> player2Opt = usersDAO.getUserById(gameModel.getPlayer2());
            if (!player2Opt.isPresent()) {
                throw new IllegalStateException(
                        format("Player 2 (id=%d) from game (id=%d) doesn't exist in the database!", gameModel.getPlayer2(), gameModel.getId()));
            }
            gameModel.setPlayer2Model(player2Opt.get());
        } else if (gameModel.getPlayer2().equals(authUser.getId())) {
            gameModel.setPlayer2Model(authUser);
            Optional<UserModel> player1Opt = usersDAO.getUserById(gameModel.getPlayer1());
            if (!player1Opt.isPresent()) {
                throw new IllegalStateException(
                        format("Player 1 (id=%d) from game (id=%d) doesn't exist in the database!", gameModel.getPlayer1(), gameModel.getId()));
            }
            gameModel.setPlayer1Model(player1Opt.get());
        } else {
            throw new IllegalStateException(format("The auth user (id=%d) isn't player1 or player2 for the given game: %s", authUser.getId(), gameModel));
        }
    }
}
