package net.capps.word.rest.services;

import com.google.common.base.Optional;
import net.capps.word.exceptions.ConflictException;
import net.capps.word.models.ErrorModel;
import net.capps.word.models.UserModel;
import net.capps.word.rest.providers.UsersProvider;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static javax.ws.rs.core.Response.Status.*;

/**
 * Created by charlescapps on 12/27/14.
 */
@Path("/users")
@Produces(value = "application/json")
@Consumes(value = "application/json")
public class UsersService {
    private static final UsersProvider usersProvider = UsersProvider.getInstance();

    @Context
    private UriInfo uriInfo;

    @POST
    public Response createUser(@Context HttpServletRequest request, UserModel inputUser) throws Exception {

        Optional<ErrorModel> validationError = usersProvider.validateInputUser(inputUser);
        if (validationError.isPresent()) {
            return Response.status(BAD_REQUEST)
                    .entity(validationError.get())
                    .build();
        }

        try {
            UserModel createdUser = usersProvider.createNewUser(inputUser);

            URI uri = getWordUserURI(createdUser.getId());
            return Response.created(uri)
                    .entity(createdUser)
                    .build();
        } catch (ConflictException e) {
            return Response.status(CONFLICT)
                    .entity(new ErrorModel(e.getMessage()))
                    .build();
        }
    }

    @Path("/{id}")
    public Response getUser(@PathParam("id") int id) throws Exception {
        Optional<UserModel> result = usersProvider.getUserById(id);
        if (!result.isPresent()) {
            return Response.status(NOT_FOUND)
                    .entity(new ErrorModel("No user exists with id " + id))
                    .build();
        }
        return Response.ok(result.get())
                .build();
    }


    // ------ Helpers ----
    public URI getWordUserURI(int id) {
        return uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(id))
                .build();
    }


}
