package net.capps.word.rest.services;

import net.capps.word.rest.auth.AuthHelper;
import net.capps.word.rest.filters.Filters;
import net.capps.word.rest.models.ErrorModel;
import net.capps.word.rest.models.PurchaseModel;
import net.capps.word.rest.models.UserModel;
import net.capps.word.rest.providers.TokensProvider;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
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

    @Path("/purchase")
    @POST
    @Filters.RegularUserAuthRequired
    public Response grantTokensForPurchase(@Context HttpServletRequest request, PurchaseModel purchaseModel)
            throws SQLException {
        UserModel authUser = (UserModel) request.getAttribute(AuthHelper.AUTH_USER_PROPERTY);
        if (authUser == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<ErrorModel> optError = tokensProvider.validatePurchase(purchaseModel);
        if (optError.isPresent()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(optError.get())
                    .build();
        }

        UserModel updatedUser = tokensProvider.giveTokensForPurchase(authUser, purchaseModel.getProduct());
        return Response.ok(updatedUser).build();
    }

}
