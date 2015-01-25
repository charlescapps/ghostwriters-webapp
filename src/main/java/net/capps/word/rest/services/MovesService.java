package net.capps.word.rest.services;

/**
 * Created by charlescapps on 1/24/15.
 */

import net.capps.word.rest.models.MoveModel;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.ws.Response;

@Path("moves")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MovesService {

    @POST
    public Response playMove(MoveModel input) {

    }
}
