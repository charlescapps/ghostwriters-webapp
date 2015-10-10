package net.capps.word.rest.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * Created by charlescapps on 10/10/15.
 */
@Path("facebook")
@Produces(MediaType.TEXT_HTML)
public class FacebookCanvasService {
    private static final Logger LOG = LoggerFactory.getLogger(FacebookCanvasService.class);

    @Context
    private UriInfo uriInfo;

    @POST
    @Path("{fbData}")
    public Response getFacebookCanvas(@PathParam("fbData") String fbData) {
        LOG.info("fbData={}", fbData);
        URI uri = uriInfo.getBaseUriBuilder()
                .replacePath("/resources/html/facebook_canvas.html")
                .build();
        return Response.seeOther(uri).build();
    }
}
