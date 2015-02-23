package net.capps.word.rest.services;

/**
 * Created by charlescapps on 1/24/15.
 */

import com.google.common.base.Optional;
import net.capps.word.game.common.GameType;
import net.capps.word.rest.auth.AuthHelper;
import net.capps.word.rest.filters.Filters;
import net.capps.word.rest.models.ErrorModel;
import net.capps.word.rest.models.GameModel;
import net.capps.word.rest.models.MoveModel;
import net.capps.word.rest.models.UserModel;
import net.capps.word.rest.providers.MovesProvider;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status;

@Path("moves")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Filters.RegularUserAuthRequired
public class MovesService {
    private static final AuthHelper authHelper = AuthHelper.getInstance();

    @POST
    public Response playMove(@Context HttpServletRequest request, MoveModel input) throws Exception {
        Optional<UserModel> authUserOpt = authHelper.validateSession(request);
        if (!authUserOpt.isPresent()) {
            return Response.status(Status.UNAUTHORIZED)
                    .entity(new ErrorModel("Must be authenticated as a regular user to send a Move."))
                    .build();
        }
        UserModel authUser = authUserOpt.get();
        Optional<ErrorModel> errorOpt = MovesProvider.getInstance().validateMove(input, authUser);
        if (errorOpt.isPresent()) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(errorOpt.get())
                    .build();
        }
        GameModel updatedGame = MovesProvider.getInstance().playMove(input);

        // For single player games, immediately play the AI's move and return the updated game.
        if (updatedGame.getGameType() == GameType.SINGLE_PLAYER) {
            updatedGame = MovesProvider.getInstance().playAIMove(updatedGame.getAiType(), updatedGame, input);
        }

        return Response.ok(updatedGame).build();
    }
}
