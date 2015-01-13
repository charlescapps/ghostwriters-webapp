package net.capps.word.rest.filters;

import com.google.common.base.Optional;
import net.capps.word.models.UserModel;
import net.capps.word.rest.auth.AuthHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

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
            if (requestContext.getUriInfo().getPath().endsWith("/login")) {
                authHelper.validateSessionLoggedInAsInitialUser(webRequest);
                return;
            }

            Optional<UserModel> authUser = authHelper.validateSession(webRequest);
            if (!authUser.isPresent()) {
                LOG.warn("Unauthorized {} to {}", webRequest.getMethod(), webRequest.getPathInfo());
                requestContext.abortWith(Response.status(UNAUTHORIZED)
                        .entity(authUser.get())
                        .build());
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            requestContext.abortWith(Response.status(INTERNAL_SERVER_ERROR).build());
        }

    }
}