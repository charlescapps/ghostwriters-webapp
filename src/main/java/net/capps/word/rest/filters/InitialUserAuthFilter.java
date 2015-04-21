package net.capps.word.rest.filters;

import net.capps.word.constants.WordConstants;
import net.capps.word.exceptions.WordAuthException;
import net.capps.word.rest.auth.AuthHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

import static javax.ws.rs.core.Response.Status;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

/**
 * Authentication filter requiring user to be logged in as the Initial User.
 * Used for the endpoint to create a new user.
 */
@Provider
@Filters.InitialUserAuthRequired
public class InitialUserAuthFilter implements ContainerRequestFilter {
    private static final AuthHelper authHelper = AuthHelper.getInstance();
    private static final Logger LOG = LoggerFactory.getLogger(InitialUserAuthFilter.class);

    @Context
    HttpServletRequest webRequest;
 
    @Override
    public void filter(ContainerRequestContext requestContext)
                    throws IOException {

        try {
            // For creating a new user, must be logged in as the Initial user using basic auth
            // We can't use sessions here...because the Initial User is shared among all clients.
            Pair<String, String> usernamePass = authHelper.getUsernamePassFromAuthzHeader(webRequest);

            if (!WordConstants.INITIAL_USER_USERNAME.equals(usernamePass.getLeft()) ||
                !WordConstants.INITIAL_USER_PASSWORD.equals(usernamePass.getRight())) {
                requestContext.abortWith(Response.status(Status.UNAUTHORIZED).build());
            }

        } catch (WordAuthException e) {
          requestContext.abortWith(Response.status(Status.UNAUTHORIZED).build());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            requestContext.abortWith(Response.status(INTERNAL_SERVER_ERROR).build());
        }

    }
}