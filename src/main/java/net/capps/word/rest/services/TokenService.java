package net.capps.word.rest.services;

import net.capps.word.db.dao.UsersDAO;
import net.capps.word.rest.auth.AuthHelper;
import net.capps.word.rest.filters.Filters;
import net.capps.word.rest.models.ErrorModel;
import net.capps.word.rest.models.UserModel;
import net.capps.word.rest.providers.TokensProvider;
import net.capps.word.rest.providers.UsersProvider;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

/**
 * Created by charlescapps on 5/12/15.
 */
@Path(TokenService.TOKEN_PATH)
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
public class TokenService {
    public static final String TOKEN_PATH = "tokens";
    private static final TokensProvider tokensProvider = TokensProvider.getInstance();

    @Path("spendTokens")
    @POST
    @Filters.RegularUserAuthRequired
    public Response spendTokens(@Context HttpServletRequest request, @QueryParam("numTokens") Integer numTokens)
            throws Exception {
        UserModel authUser = (UserModel) request.getAttribute(AuthHelper.AUTH_USER_PROPERTY);
        if (authUser == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        // Validation
        Optional<ErrorModel> tokensErrOpt = tokensProvider.validateNumTokens(numTokens);
        if (tokensErrOpt.isPresent()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(tokensErrOpt.get())
                    .build();
        }

        // Update the user's tokens and return OK
        UserModel updatedUser = tokensProvider.spendTokens(authUser, numTokens);
        return Response.ok(updatedUser).build();
    }

}
