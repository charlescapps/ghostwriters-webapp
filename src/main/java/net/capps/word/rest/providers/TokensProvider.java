package net.capps.word.rest.providers;

import net.capps.word.db.dao.UsersDAO;
import net.capps.word.game.dict.GameDict;
import net.capps.word.rest.models.ErrorModel;
import net.capps.word.rest.models.GameModel;
import net.capps.word.rest.models.UserModel;

import javax.ws.rs.BadRequestException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static java.lang.String.format;

/**
 * Created by charlescapps on 5/12/15.
 */
public class TokensProvider {
    private static final UsersDAO usersDAO = UsersDAO.getInstance();
    private static final TokensProvider INSTANCE = new TokensProvider();

    // Errors
    private static final ErrorModel MISSING_NUM_TOKENS = new ErrorModel("Missing numTokens query param");
    private static final ErrorModel INVALID_NUM_TOKENS = new ErrorModel("Invalid numTokens, must be positive");

    private TokensProvider() { }

    public static TokensProvider getInstance() {
        return INSTANCE;
    }

    public Optional<ErrorModel> validateNumTokens(Integer numTokens) {
        if (numTokens == null) {
            return Optional.of(MISSING_NUM_TOKENS);
        }
        if (numTokens <= 0) {
            return Optional.of(INVALID_NUM_TOKENS);
        }
        return Optional.empty();
    }

    public UserModel spendTokens(UserModel userModel, int numTokens, Connection dbConn) throws SQLException {
        if (userModel.getTokens() < numTokens) {
            throw new BadRequestException(
                    format("You only have %d tokens, you cannot afford to spend {} tokens!", userModel.getTokens(), numTokens));
        }
        return usersDAO.spendTokens(dbConn, userModel.getId(), numTokens);
    }

    public UserModel spendTokensForCreateGame(UserModel authUser, GameModel inputGame, Connection dbConn) throws SQLException {
        int cost = computeCreateGameTokenCost(inputGame);
        return spendTokens(authUser, cost, dbConn);
    }

    public Optional<ErrorModel> getCanAffordGameError(UserModel authUser, GameModel validatedInputGame) {
        int currentTokens = authUser.getTokens();
        int requiredTokens = computeCreateGameTokenCost(validatedInputGame);

        if (requiredTokens > currentTokens) {
            return Optional.of(new ErrorModel(
                    String.format("You can't afford to spend %d tokens creating this game.", requiredTokens)));
        }

        return Optional.empty();
    }

    private int computeCreateGameTokenCost(GameModel inputGame) {
        int cost = 0;
        GameDict specialDict = inputGame.getSpecialDict();
        if (specialDict != null) {
            cost += specialDict.getTokenCost();
        }

        return cost + inputGame.getBoardSize().getTokenCost();
    }
}
