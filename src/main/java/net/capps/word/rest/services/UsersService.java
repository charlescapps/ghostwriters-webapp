package net.capps.word.rest.services;

import com.google.common.base.Optional;
import net.capps.word.models.ErrorModel;
import net.capps.word.models.UserModel;
import net.capps.word.rest.providers.UsersProvider;

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
public class UsersService {
    private final UsersProvider usersProvider = new UsersProvider();

    @Context
    private UriInfo uriInfo;

    @POST
    public Response createUser(UserModel inputUser) throws Exception {
        Optional<ErrorModel> validationError = usersProvider.validateInputUser(inputUser);
        if (validationError.isPresent()) {
            return Response.status(BAD_REQUEST)
                    .entity(validationError.get())
                    .build();
        }

        UserModel createdUser = usersProvider.createNewUser(inputUser);
        URI uri = getWordUserURI(createdUser.getId());
        return Response.created(uri)
                .entity(createdUser)
                .build();
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
