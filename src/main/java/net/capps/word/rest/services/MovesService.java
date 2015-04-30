package net.capps.word.rest.services;

/**
 * Created by charlescapps on 1/24/15.
 */

import com.google.common.base.Optional;
import net.capps.word.db.WordDbManager;
import net.capps.word.game.common.GameType;
import net.capps.word.rest.auth.AuthHelper;
import net.capps.word.rest.filters.Filters;
import net.capps.word.rest.models.ErrorModel;
import net.capps.word.rest.models.GameModel;
import net.capps.word.rest.models.MoveModel;
import net.capps.word.rest.models.UserModel;
import net.capps.word.rest.providers.MovesProvider;
import net.capps.word.rest.providers.OneSignalProvider;
import net.capps.word.rest.providers.RatingsProvider;
import net.capps.word.util.ErrorOrResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;

import static javax.ws.rs.core.Response.Status;

@Path("moves")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Filters.RegularUserAuthRequired
public class MovesService {
    private static final MovesProvider movesProvider = MovesProvider.getInstance();
    private static final RatingsProvider ratingsProvider = RatingsProvider.getInstance();
    private static final OneSignalProvider oneSignalProvider = OneSignalProvider.getInstance();
    private static final Logger LOG = LoggerFactory.getLogger(MovesService.class);

    @POST
    public Response playMove(@Context HttpServletRequest request, MoveModel input) throws Exception {
        UserModel authUser = (UserModel) request.getAttribute(AuthHelper.AUTH_USER_PROPERTY);
        if (authUser == null) {
            return Response.status(Status.UNAUTHORIZED)
                    .entity(new ErrorModel("You must login to send a move."))
                    .build();
        }
        ErrorOrResult<GameModel> errorOrResult = movesProvider.validateMove(input, authUser);

        Optional<ErrorModel> errorOpt = errorOrResult.getError();
        if (errorOpt.isPresent()) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(errorOpt.get())
                    .build();
        }

        final GameModel originalGame = errorOrResult.getResult().get();

        // We need to rollback if a failure occurs, so use a common database connection with autoCommit == false
        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            dbConn.setAutoCommit(false);

            GameModel updatedGame = movesProvider.playMove(input, originalGame, dbConn);
            boolean isAiTurn = updatedGame.getPlayer1().equals(authUser.getId()) && !updatedGame.getPlayer1Turn() ||
                               updatedGame.getPlayer2().equals(authUser.getId()) && updatedGame.getPlayer1Turn();

            // For single player games, play the AI's move if the turn changed
            if (updatedGame.getGameType() == GameType.SINGLE_PLAYER && isAiTurn) {
                LOG.info("Playing AI moves...");
                updatedGame = movesProvider.playAIMoves(updatedGame.getAiType(), updatedGame, input, dbConn);
            } else {
                movesProvider.populateLastMoves(updatedGame, originalGame, input, dbConn);
            }

            ratingsProvider.updatePlayerRatings(updatedGame, dbConn);

            dbConn.commit();

            try {
                oneSignalProvider.sendPushNotificationForMove(originalGame, updatedGame);
            } catch (Throwable t) {
                LOG.warn("An error occurred trying to send a push notification to One Signal:", t);
            }

            return Response.ok(updatedGame).build();
        }

    }
}
