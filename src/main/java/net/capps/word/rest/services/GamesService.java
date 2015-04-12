package net.capps.word.rest.services;

import com.google.common.base.Optional;
import net.capps.word.db.WordDbManager;
import net.capps.word.db.dao.GamesDAO;
import net.capps.word.rest.auth.AuthHelper;
import net.capps.word.rest.filters.Filters;
import net.capps.word.rest.models.ErrorModel;
import net.capps.word.rest.models.GameListModel;
import net.capps.word.rest.models.GameModel;
import net.capps.word.rest.models.UserModel;
import net.capps.word.rest.providers.GamesProvider;
import net.capps.word.rest.providers.GamesSearchProvider;
import net.capps.word.rest.providers.MovesProvider;

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

    @Context
    private UriInfo uriInfo;

    @POST
    public Response createNewGame(@Context HttpServletRequest request, GameModel input) throws Exception {
        UserModel player1 = (UserModel) request.getAttribute(AuthHelper.AUTH_USER_PROPERTY);
        if (player1 == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        Optional<ErrorModel> errorOpt = gamesProvider.validateInputForCreateGame(input, player1);
        if (errorOpt.isPresent()) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(errorOpt.get())
                    .build();
        }

        GameModel created = gamesProvider.createNewGame(input, player1);

        URI uri = gamesProvider.getGameURI(created.getId(), uriInfo);

        return Response.created(uri)
                .entity(created)
                .build();
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
                return Response.ok().build();
            }

            if (Boolean.TRUE.equals(includeMoves)) {
                movesProvider.populateLastMoves(gameModel, authUser, dbConn);
            }
            return Response.ok(gameModel).build();
        }
    }

    @GET
    public Response getMyGames(@Context HttpServletRequest request,
                               @QueryParam("count") Integer count,
                               @QueryParam("inProgress") Boolean inProgress,
                               @QueryParam("includeMoves") Boolean includeMoves) throws SQLException {
        UserModel authUser = (UserModel) request.getAttribute(AuthHelper.AUTH_USER_PROPERTY);
        if (authUser == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        Optional<ErrorModel> error = gamesSearchProvider.validateSearchParams(count, inProgress);
        if (error.isPresent()) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(error.get())
                    .build();
        }
        try (Connection dbConn = WORD_DB_MANAGER.getConnection()) {
            boolean doIncludeMoves = Boolean.TRUE.equals(includeMoves);
            List<GameModel> games = gamesSearchProvider.getGamesForUserLastActivityDesc(authUser, count, inProgress, doIncludeMoves, dbConn);
            return Response.ok(new GameListModel(games)).build();
        }
    }
}
