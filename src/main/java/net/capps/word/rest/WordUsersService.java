package net.capps.word.rest;

import net.capps.word.models.WordUserModel;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by charlescapps on 12/27/14.
 */
@Path("/users")
public class WordUsersService {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(WordUserModel inputUser) {
        return Response.status(Response.Status.BAD_REQUEST).build();
    }
}
