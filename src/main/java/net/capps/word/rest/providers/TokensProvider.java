package net.capps.word.rest.providers;

import net.capps.word.db.dao.UsersDAO;
import net.capps.word.game.dict.SpecialDict;
import net.capps.word.iap.InAppPurchaseProduct;
import net.capps.word.rest.models.ErrorModel;
import net.capps.word.rest.models.GameModel;
import net.capps.word.rest.models.PurchaseModel;
import net.capps.word.rest.models.UserModel;

import javax.ws.rs.BadRequestException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.regex.Matcher;

import static java.lang.String.format;

/**
 * Created by charlescapps on 5/12/15.
 */
public class TokensProvider {
    private static final UsersDAO usersDAO = UsersDAO.getInstance();
    private static final TokensProvider INSTANCE = new TokensProvider();

    private TokensProvider() { }

    public static TokensProvider getInstance() {
        return INSTANCE;
    }

    public Optional<ErrorModel> validatePurchase(PurchaseModel purchaseModel) {
        if (purchaseModel.getIsGoogle() == null) {
            return Optional.of(new ErrorModel("A purchase must include the 'isGoogle' field."));
        }
        if (purchaseModel.getIdentifier() == null) {
            return Optional.of(new ErrorModel("A purchase must include a valid identifier string."));
        }
        if (purchaseModel.getIsGoogle() && purchaseModel.getSignature() == null) {
            return Optional.of(new ErrorModel("A purchase must include a valid signature string."));
        }
        if (purchaseModel.getProduct() == null) {
            return Optional.of(new ErrorModel("A purchase must provide a product type."));
        }
        return Optional.empty();
    }

    public UserModel giveTokensForPurchase(UserModel userModel, InAppPurchaseProduct product) throws SQLException {
        switch (product) {
            case infinite_books:
                return usersDAO.grantInfiniteBooks(userModel.getId());
            default:
                return usersDAO.increaseUsersTokensForPurchase(userModel.getId(), product.getNumTokens());
        }
    }

    public UserModel spendTokens(UserModel userModel, int numTokens, Connection dbConn) throws SQLException {
        if (userModel.getTokens() < numTokens) {
            throw new BadRequestException(
                    format("You only have %d tokens, you cannot afford to spend {} tokens!", userModel.getTokens(), numTokens));
        }
        return usersDAO.spendTokens(dbConn, userModel.getId(), numTokens);
    }

    public UserModel spendTokensForCreateGame(UserModel authUser, GameModel inputGame, Connection dbConn) throws SQLException {
        // If the user has purchased infinite_books, then do nothing; the game is free!
        if (Boolean.TRUE.equals(authUser.getInfiniteBooks())) {
            return authUser;
        }
        int cost = computeCreateGameTokenCost(inputGame);
        return spendTokens(authUser, cost, dbConn);
    }

    public Optional<ErrorModel> getCanAffordGameError(UserModel authUser, GameModel validatedInputGame) {
        int requiredTokens = computeCreateGameTokenCost(validatedInputGame);
        int currentTokens = authUser.getTokens();

        if (requiredTokens > currentTokens) {
            return Optional.of(new ErrorModel(
                    String.format("You can't afford to spend %d tokens creating this game.", requiredTokens)));
        }

        return Optional.empty();
    }

    private int computeCreateGameTokenCost(GameModel inputGame) {
        int cost = 0;
        SpecialDict specialDict = inputGame.getSpecialDict();
        if (specialDict != null) {
            cost += specialDict.getTokenCost();
        }

        String initialRack = inputGame.getPlayer1Rack();
        if (initialRack != null) {
            Matcher m = GamesProvider.INITIAL_RACK_PATTERN.matcher(initialRack);
            if (!m.matches()) {
                throw new IllegalStateException(format("Invalid initial rack: '%s'", initialRack));
            }
            int numBlankTiles = initialRack.length();
            cost += numBlankTiles; // Add the cost of 1 book per blank tile.
        }

        // Add the cost of the board size.
        return cost + inputGame.getBoardSize().getTokenCost();
    }
}
