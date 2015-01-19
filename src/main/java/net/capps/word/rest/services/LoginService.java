package net.capps.word.rest.services;

import net.capps.word.exceptions.WordAuthException;
import net.capps.word.rest.auth.AuthHelper;
import net.capps.word.rest.models.ErrorModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * Created by charlescapps on 1/4/15.
 */
@Path(LoginService.LOGIN_PATH)
public class LoginService {
    public static final String LOGIN_PATH = "/login";
    private static final AuthHelper authHelper = AuthHelper.getInstance();
    private static final Logger LOG = LoggerFactory.getLogger(LoginService.class);

    @POST
    public Response login(@Context HttpServletRequest request) throws Exception {
        try {
            authHelper.loginUsingBasicAuth(request);
        } catch (WordAuthException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorModel(e.getMessage()))
                    .build();
        }
        return Response.ok().build();
    }
}
