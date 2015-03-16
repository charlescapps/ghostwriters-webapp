package net.capps.word.rest.services;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import net.capps.word.db.dao.UsersDAO;
import net.capps.word.exceptions.ConflictException;
import net.capps.word.game.dict.RandomUsernamePicker;
import net.capps.word.rest.filters.Filters;
import net.capps.word.rest.models.*;
import net.capps.word.rest.providers.SessionProvider;
import net.capps.word.rest.providers.UsersProvider;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.sql.SQLException;
import java.util.List;

import static javax.ws.rs.core.Response.Status.*;

/**
 * Created by charlescapps on 12/27/14.
 */
@Path(UsersService.USERS_PATH)
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
public class UsersService {
    public static final String USERS_PATH = "users";
    private static final UsersProvider usersProvider = UsersProvider.getInstance();
    private static final SessionProvider sessionProvider = SessionProvider.getInstance();

    @Context
    private UriInfo uriInfo;

    @POST
    @Filters.InitialUserAuthRequired
    public Response createUser(@Context HttpServletRequest request, UserModel inputUser) throws Exception {

        Optional<ErrorModel> validationError = usersProvider.validateInputUser(inputUser);
        if (validationError.isPresent()) {
            return Response.status(BAD_REQUEST)
                    .entity(validationError.get())
                    .build();
        }

        try {
            UserModel createdUser = usersProvider.createNewUser(inputUser);
            SessionModel session = sessionProvider.createNewSession(createdUser);

            URI uri = getWordUserURI(createdUser.getId());
            return Response.created(uri)
                    .entity(createdUser)
                    .cookie(session.getNewCookie())
                    .build();
        } catch (ConflictException e) {
            return Response.status(CONFLICT)
                    .entity(new ErrorModel(e.getMessage()))
                    .build();
        }
    }

    @Path("/{id}")
    @GET
    @Filters.RegularUserAuthRequired
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

    @Path("/nextUsername")
    @GET
    @Filters.InitialUserAuthRequired
    public Response getNextUsername(@QueryParam("deviceId") String deviceId) throws SQLException {
        if (!Strings.isNullOrEmpty(deviceId)) {
            Optional<UserModel> existingUser = UsersDAO.getInstance().getUserByDeviceId(deviceId);
            if (existingUser.isPresent()) {
                return Response.ok(new NextUsernameModel(existingUser.get().getUsername(), true))
                        .build();
            }
        }
        Optional<String> randomUsername = RandomUsernamePicker.getInstance().generateRandomUsername();
        String nextUsername = randomUsername.isPresent() ? randomUsername.get() : null;
        return Response.ok(new NextUsernameModel(nextUsername, false)).build();
    }

    @GET
    @Filters.RegularUserAuthRequired
    public Response searchUsers(@QueryParam("q") String q, @QueryParam("maxResults") int maxResults)
            throws Exception {
        if (Strings.isNullOrEmpty(q)) {
            return Response.status(BAD_REQUEST)
                    .entity(new ErrorModel("Must provide the query param \"q\" to search for users."))
                    .build();
        }
        if (maxResults <= 0) {
            return Response.status(BAD_REQUEST)
                    .entity(new ErrorModel("Must provide the query param \"maxResults\" and it must be > 0"))
                    .build();
        }
        List<UserModel> results = usersProvider.searchUsers(q, maxResults);
        return Response.ok(new UserListModel(results)).build();
    }


    // ------ Helpers ----
    public URI getWordUserURI(int id) {
        return uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(id))
                .build();
    }


}
