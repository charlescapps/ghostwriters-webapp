package net.capps.word.rest.services;

import net.capps.word.db.WordDbManager;
import net.capps.word.rest.auth.AuthHelper;
import net.capps.word.rest.filters.Filters;
import net.capps.word.rest.models.GameModel;
import net.capps.word.rest.models.MoveModel;
import net.capps.word.rest.models.UserModel;
import net.capps.word.rest.providers.SpecialActionsProvider;
import net.capps.word.util.ErrorOrResult;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;

/**
 * Created by charlescapps on 6/24/15.
 */
@Path(SpecialActionService.PATH)
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
public class SpecialActionService {
    public static final String PATH = "specialActions";

    private static final SpecialActionsProvider specialActionsProvider = SpecialActionsProvider.getInstance();

    @Path("scry")
    @POST
    @Filters.RegularUserAuthRequired
    public Response grantTokensForPurchase(@Context HttpServletRequest request, @QueryParam("gameId") Integer gameId)
            throws Exception {
        UserModel authUser = (UserModel) request.getAttribute(AuthHelper.AUTH_USER_PROPERTY);
        if (authUser == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            dbConn.setAutoCommit(false);

            ErrorOrResult<GameModel> errorOrGame = specialActionsProvider.validateScryAction(gameId, authUser, dbConn);

            // Validate the game against the authenticated user
            if (errorOrGame.isError()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorOrGame.getErrorOpt().get())
                        .build();
            }

            GameModel gameModel = errorOrGame.getResultOpt().get();

            ErrorOrResult<MoveModel> scryMoveOrError = specialActionsProvider.getOracleMoveAndUpdateUserRack(gameModel, authUser, dbConn);

            if (scryMoveOrError.isError()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(scryMoveOrError.getErrorOpt().get())
                        .build();
            }

            dbConn.commit();

            return Response.ok(scryMoveOrError.getResultOpt().get()).build();
        }

    }
}
