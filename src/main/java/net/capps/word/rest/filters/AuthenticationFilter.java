package net.capps.word.rest.filters;

import com.google.common.base.Optional;
import net.capps.word.constants.WordConstants;
import net.capps.word.rest.auth.AuthHelper;
import net.capps.word.rest.models.UserModel;
import net.capps.word.rest.services.LoginService;
import net.capps.word.rest.services.UsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.URI;

import static javax.ws.rs.core.Response.Status;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

@Provider
public class AuthenticationFilter implements ContainerRequestFilter {
    private static final AuthHelper authHelper = AuthHelper.getInstance();
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Context
    HttpServletRequest webRequest;
 
    @Override
    public void filter(ContainerRequestContext requestContext)
                    throws IOException {

        try {
            final URI absolutePath = requestContext.getUriInfo().getAbsolutePath();

            // Don't need to be authenticated to login
            if (absolutePath.toString().endsWith(LoginService.LOGIN_PATH)) {
                return;
            }

            // For creating a new user, must be logged in as the Initial user
            if (absolutePath.toString().endsWith(UsersService.USERS_PATH) &&
                    requestContext.getMethod().equals(HttpMethod.POST)) {
                Optional<UserModel> authUser = authHelper.validateSession(webRequest);
                if (!authUser.isPresent()) {
                    requestContext.abortWith(Response.status(Status.UNAUTHORIZED).build());
                    return;
                }
                if (!WordConstants.INITIAL_USER_USERNAME.equals(authUser.get().getUsername())) {
                    requestContext.abortWith(Response.status(Status.FORBIDDEN).build());
                    return;
                }
                // Successfully authenticated as Initial User.
                return;
            }

            Optional<UserModel> authUser = authHelper.validateSession(webRequest);
            if (!authUser.isPresent()) {
                LOG.warn("Unauthorized {} to {}", webRequest.getMethod(), webRequest.getPathInfo());
                requestContext.abortWith(Response.status(UNAUTHORIZED).build());
                return;
            }
            if (WordConstants.INITIAL_USER_USERNAME.equals(authUser.get().getUsername())) {
                LOG.warn("Forbidden {} to {} as Initial User", webRequest.getMethod(), webRequest.getPathInfo());
                requestContext.abortWith(Response.status(FORBIDDEN).build());
                return;
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            requestContext.abortWith(Response.status(INTERNAL_SERVER_ERROR).build());
        }

    }
}