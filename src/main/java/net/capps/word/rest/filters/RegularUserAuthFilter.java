package net.capps.word.rest.filters;

import com.google.common.base.Optional;
import net.capps.word.constants.WordConstants;
import net.capps.word.rest.auth.AuthHelper;
import net.capps.word.rest.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

import static javax.ws.rs.core.Response.Status.*;

@Provider
@Filters.RegularUserAuthRequired
public class RegularUserAuthFilter implements ContainerRequestFilter {
    private static final AuthHelper authHelper = AuthHelper.getInstance();
    private static final Logger LOG = LoggerFactory.getLogger(RegularUserAuthFilter.class);

    @Context
    HttpServletRequest webRequest;
 
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        try {
            Optional<UserModel> authUser = authHelper.validateSession(webRequest);
            if (!authUser.isPresent()) {
                LOG.warn("Unauthorized {} to {} from IP {}",
                        webRequest.getMethod(),
                        webRequest.getPathInfo(),
                        webRequest.getRemoteAddr());
                requestContext.abortWith(Response.status(UNAUTHORIZED).build());
                return;
            }
            if (WordConstants.INITIAL_USER_USERNAME.equals(authUser.get().getUsername())) {
                LOG.warn("Forbidden {} to {} as Initial User from IP {}",
                        webRequest.getMethod(),
                        webRequest.getPathInfo(),
                        webRequest.getRemoteAddr());

                requestContext.abortWith(Response.status(FORBIDDEN).build());
                return;
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            requestContext.abortWith(Response.status(INTERNAL_SERVER_ERROR).build());
        }

    }
}