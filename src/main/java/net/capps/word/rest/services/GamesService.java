package net.capps.word.rest.services;

import com.google.common.base.Optional;
import net.capps.word.db.dao.GamesDAO;
import net.capps.word.rest.auth.AuthHelper;
import net.capps.word.rest.filters.Filters;
import net.capps.word.rest.models.ErrorModel;
import net.capps.word.rest.models.GameModel;
import net.capps.word.rest.models.ListModel;
import net.capps.word.rest.models.UserModel;
import net.capps.word.rest.providers.GamesProvider;
import net.capps.word.rest.providers.GamesSearchProvider;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
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
    private static final AuthHelper authHelper = AuthHelper.getInstance();
    private static final GamesProvider gamesProvider = GamesProvider.getInstance();
    private static final GamesSearchProvider gamesSearchProvider = GamesSearchProvider.getInstance();

    @Context
    private UriInfo uriInfo;

    @POST
    public Response createNewGame(@Context HttpServletRequest request, GameModel input) throws Exception {
        Optional<UserModel> authUserOpt = authHelper.validateSession(request);
        if (!authUserOpt.isPresent()) {
            return Response.status(Status.UNAUTHORIZED)
                    .entity(new ErrorModel("You must be logged in to create a new game!"))
                    .build();
        }

        UserModel player1 = authUserOpt.get();
        Optional<ErrorModel> errorOpt = gamesProvider.validateInputForCreateGame(input, player1);
        if (errorOpt.isPresent()) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(errorOpt.get())
                    .build();
        }

        GameModel created = gamesProvider.createNewGame(input, authUserOpt.get());

        URI uri = gamesProvider.getGameURI(created.getId(), uriInfo);

        return Response.created(uri)
                .entity(created)
                .build();
    }

    @GET
    @Path("/{id}")
    public Response getGameById(@PathParam("id") int id) throws Exception {
        Optional<GameModel> game = GamesDAO.getInstance().getGameById(id);
        if (game.isPresent()) {
            return Response.ok(game.get()).build();
        }
        return Response.status(Status.NOT_FOUND)
                .entity(new ErrorModel(String.format("No game found with id %d", id)))
                .build();
    }

    @GET
    public Response getGamesForUser(@QueryParam("userId") Integer userId,
                                    @QueryParam("count") Integer count,
                                    @QueryParam("inProgress") Boolean inProgress) throws SQLException {
        Optional<ErrorModel> error = gamesSearchProvider.validateSearchParams(userId, count, inProgress);
        if (error.isPresent()) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(error.get())
                    .build();
        }

        List<GameModel> games = gamesSearchProvider.getGamesForUser(userId, count, inProgress);
        return Response.ok(new ListModel<>(games)).build();
    }
}
