package net.capps.word.rest.services;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
    @Context
    private UriInfo uriInfo;

    @POST
    public Response getFacebookCanvas() {
        URI uri = uriInfo.getBaseUriBuilder()
                .replacePath("/resources/html/facebook_canvas.html")
                .build();
        return Response.seeOther(uri).build();
    }
}