package net.capps.word.rest.services;

import net.capps.word.db.WordDbManager;
import net.capps.word.db.dao.GamesDAO;
import net.capps.word.rest.auth.AuthHelper;
import net.capps.word.rest.filters.Filters;
import net.capps.word.rest.models.*;
import net.capps.word.rest.providers.GamesProvider;
import net.capps.word.rest.providers.GamesSearchProvider;
import net.capps.word.rest.providers.MovesProvider;
import net.capps.word.rest.providers.TokensProvider;
import net.capps.word.util.ErrorOrResult;
import net.capps.word.util.RestUtil;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static javax.ws.rs.core.Response.Status;

/**
 * Created by charlescapps on 1/18/15.
 */
@Path(GamesService.GAMES_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Filters.RegularUserAuthRequired
public class GamesService {
    public static final String GAMES_PATH = "games";
    private static final WordDbManager WORD_DB_MANAGER = WordDbManager.getInstance();
    private static final GamesDAO gamesDAO = GamesDAO.getInstance();
    private static final GamesProvider gamesProvider = GamesProvider.getInstance();
    private static final GamesSearchProvider gamesSearchProvider = GamesSearchProvider.getInstance();
    private static final MovesProvider movesProvider = MovesProvider.getInstance();
    private static final TokensProvider tokensProvider = TokensProvider.getInstance();

    private static final String DEFAULT_COUNT_STR = "25";

    // ------ OK results ------
    private static final GenericOkModel OK_REJECTED_GAME = new GenericOkModel("Game rejected!");

    @Context
    private UriInfo uriInfo;

    @POST
    public Response createNewGame(@Context HttpServletRequest request, GameModel input) throws Exception {
        UserModel player1 = (UserModel) request.getAttribute(AuthHelper.AUTH_USER_PROPERTY);
        if (player1 == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            Optional<ErrorModel> errorOpt = gamesProvider.validateInputForCreateGame(dbConn, input, player1);
            if (errorOpt.isPresent()) {
                return RestUtil.badRequest(errorOpt.get());
            }

            errorOpt = tokensProvider.validateCanAffordCreateGameError(player1, input);
            if (errorOpt.isPresent()) {
                return RestUtil.badRequest(errorOpt.get());
            }

            dbConn.setAutoCommit(false);

            GameModel created = gamesProvider.createNewGame(input, player1, dbConn);

            UserModel updatedUser = tokensProvider.spendTokensForCreateGame(player1, input, dbConn);
            created.setPlayer1Model(updatedUser);

            URI uri = gamesProvider.getGameURI(created.getId(), uriInfo);

            dbConn.commit();

            return Response.created(uri)
                    .entity(created)
                    .build();
        }

    }

    @GET
    @Path("/{id}")
    public Response getGameById(@Context HttpServletRequest request,
                                @PathParam("id") int id,
                                @QueryParam("includeMoves") Boolean includeMoves,
                                @QueryParam("currentMove") Integer currentMove) throws Exception {
        UserModel authUser = (UserModel) request.getAttribute(AuthHelper.AUTH_USER_PROPERTY);
        if (authUser == null) {
            return Response.status(Status.UNAUTHORIZED)
                    .entity(new ErrorModel("You must login to perform this action."))
                    .build();
        }
        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            Optional<GameModel> gameOpt = gamesDAO.getGameWithPlayerModelsById(id, dbConn);

            if (!gameOpt.isPresent()) {
                return Response.status(Status.NOT_FOUND)
                        .entity(new ErrorModel(String.format("No game found with id %d", id)))
                        .build();
            }

            GameModel gameModel = gameOpt.get();
            if (currentMove != null && currentMove >= gameModel.getMoveNum()) {
                // Return an empty response if the current move the client has is just as recent as this move.
                return Response.ok(GameModel.EMPTY_GAME).build();
            }

            if (Boolean.TRUE.equals(includeMoves)) {
                movesProvider.populateLastMoves(gameModel, authUser, dbConn);
            }
            return Response.ok(gameModel).build();
        }
    }

    @GET
    public Response getMyGames(@Context HttpServletRequest request,
                               @QueryParam("count") @DefaultValue(DEFAULT_COUNT_STR) int count,
                               @QueryParam("page") @DefaultValue("0") int page,
                               @QueryParam("inProgress") Boolean inProgress,
                               @QueryParam("includeMoves") Boolean includeMoves
                               ) throws SQLException {
        UserModel authUser = (UserModel) request.getAttribute(AuthHelper.AUTH_USER_PROPERTY);
        if (authUser == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        Optional<ErrorModel> error = gamesSearchProvider.validateSearchParams(count, page, inProgress);
        if (error.isPresent()) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(error.get())
                    .build();
        }
        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            boolean doIncludeMoves = Boolean.TRUE.equals(includeMoves);
            GameListModel games = gamesSearchProvider.getGamesForUserLastActivityDesc(authUser, count, page, inProgress, doIncludeMoves, dbConn);
            return Response.ok(games).build();
        }
    }

    @GET
    @Path("offeredToMe")
    public Response getGamesOfferedToMe(@Context HttpServletRequest request,
                                        @QueryParam("count") @DefaultValue(DEFAULT_COUNT_STR) int count,
                                        @QueryParam("page") @DefaultValue("0") int page) throws SQLException {
        UserModel authUser = (UserModel) request.getAttribute(AuthHelper.AUTH_USER_PROPERTY);
        if (authUser == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        Optional<ErrorModel> error = gamesSearchProvider.validateCountAndPage(count, page);
        if (error.isPresent()) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(error.get())
                    .build();
        }
        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            GameListModel games = gamesSearchProvider.getGamesOfferedToUserLastActivityDesc(authUser, count, page, dbConn);
            return Response.ok(games).build();
        }
    }

    @GET
    @Path("offeredByMe")
    public Response getGamesOfferedByMe(@Context HttpServletRequest request,
                                        @QueryParam("count") Integer count) throws SQLException {
        UserModel authUser = (UserModel) request.getAttribute(AuthHelper.AUTH_USER_PROPERTY);
        if (authUser == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        Optional<ErrorModel> error = gamesSearchProvider.validateCount(count);
        if (error.isPresent()) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(error.get())
                    .build();
        }
        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            List<GameModel> games = gamesDAO.getGamesOfferedByUserLastActivityDesc(authUser.getId(), count, dbConn);
            return Response.ok(new GameListModel(games, null)).build();
        }
    }

    @POST
    @Path("/{id}/accept")
    public Response acceptGameOffer(@Context HttpServletRequest request, @PathParam("id") int id, @QueryParam("rack") String rack) throws Exception {
        UserModel authUser = (UserModel) request.getAttribute(AuthHelper.AUTH_USER_PROPERTY);
        if (authUser == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            // Turn off auto-commit
            dbConn.setAutoCommit(false);

            ErrorOrResult<GameModel> gameOrError = gamesProvider.validateAcceptGameOffer(id, rack, authUser, dbConn);
            if (gameOrError.isError()) {
                return Response.status(Status.BAD_REQUEST)
                        .entity(gameOrError.getErrorOpt().get())
                        .build();
            }

            GameModel gameModel = gameOrError.getResultOpt().get();

            Optional<ErrorModel> canAffordError = tokensProvider.validateCanAffordAcceptGameError(authUser, rack);
            if (canAffordError.isPresent()) {
                return Response.status(Status.BAD_REQUEST)
                        .entity(canAffordError.get())
                        .build();
            }

            gamesProvider.acceptGameOfferAndUpdateRack(gameModel, rack, dbConn);
            tokensProvider.spendTokensForAcceptGame(authUser, rack, dbConn);
            Optional<GameModel> updatedGame = gamesDAO.getGameWithPlayerModelsById(id, dbConn);
            if (!updatedGame.isPresent()) {
                return Response.serverError().build();
            }

            dbConn.commit();
            return Response.ok(updatedGame.get()).build();
        }
    }

    @POST
    @Path("/{id}/reject")
    public Response rejectGameOffer(@Context HttpServletRequest request, @PathParam("id") int id) throws Exception {
        UserModel authUser = (UserModel) request.getAttribute(AuthHelper.AUTH_USER_PROPERTY);
        if (authUser == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            ErrorOrResult<GameModel> validationErrorOpt = gamesProvider.validateRejectGameOffer(id, authUser, dbConn);
            if (validationErrorOpt.isError()) {
                return Response.status(Status.BAD_REQUEST)
                        .entity(validationErrorOpt.getErrorOpt().get())
                        .build();
            }

            gamesDAO.rejectGame(id, dbConn);
        }
        return Response.ok(OK_REJECTED_GAME).build();
    }
}
