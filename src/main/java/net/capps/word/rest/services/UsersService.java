package net.capps.word.rest.services;

import com.google.common.base.Strings;
import net.capps.word.constants.WordConstants;
import net.capps.word.db.WordDbManager;
import net.capps.word.db.dao.UsersDAO;
import net.capps.word.exceptions.ConflictException;
import net.capps.word.game.dict.RandomUsernamePicker;
import net.capps.word.rest.auth.AuthHelper;
import net.capps.word.rest.filters.Filters;
import net.capps.word.rest.models.*;
import net.capps.word.rest.providers.RatingsProvider;
import net.capps.word.rest.providers.SessionProvider;
import net.capps.word.rest.providers.UsersProvider;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

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
    private static final UsersDAO usersDAO = UsersDAO.getInstance();
    private static final SessionProvider sessionProvider = SessionProvider.getInstance();
    private static final RatingsProvider ratingsProvider = RatingsProvider.getInstance();
    private static final WordDbManager wordDbManager = WordDbManager.getInstance();
    
    private static final int MAX_COUNT = 200;

    @Context
    private UriInfo uriInfo;

    @POST
    @Filters.InitialUserAuthRequired
    public Response createUser(@Context HttpServletRequest request, UserModel inputUser) throws Exception {

        try (Connection dbConn = wordDbManager.getConnection()) {
            // Check if the device is already registered, then just login as this user.
            if (!Strings.isNullOrEmpty(inputUser.getDeviceId())) {
                Optional<UserModel> existingUser = usersDAO.getUserByDeviceId(dbConn, inputUser.getDeviceId());
                if (existingUser.isPresent()) {
                    if (existingUser.get().getUsername().equals(inputUser.getUsername())) {
                        SessionModel session = sessionProvider.createNewSession(dbConn, existingUser.get());
                        return Response.ok(existingUser.get())
                                .cookie(session.getNewCookie())
                                .build();
                    } else {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity(new ErrorModel("A username is already associated with your device."))
                                .build();
                    }
                }
            }

            // Otherwise, proceed with validating the new user input and creating a new user.
            Optional<ErrorModel> validationError = usersProvider.validateInputUser(inputUser);
            if (validationError.isPresent()) {
                return Response.status(BAD_REQUEST)
                        .entity(validationError.get())
                        .build();
            }

            try {
                UserModel createdUser = usersProvider.createNewUser(dbConn, inputUser);
                SessionModel session = sessionProvider.createNewSession(dbConn, createdUser);

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
    }

    @Path("/{id}")
    @GET
    @Filters.RegularUserAuthRequired
    public Response getUser(@PathParam("id") int id) throws Exception {
        try (Connection dbConn = wordDbManager.getConnection()) {
            Optional<UserModel> result = usersDAO.getUserById(dbConn, id);
            if (!result.isPresent()) {
                return Response.status(NOT_FOUND)
                        .entity(new ErrorModel("No user exists with id " + id))
                        .build();
            }
            return Response.ok(result.get())
                    .build();
        }
    }

    @Path("/me")
    @GET
    @Filters.RegularUserAuthRequired
    public Response getSelf(@Context HttpServletRequest request) throws Exception {
        UserModel authUser = (UserModel) request.getAttribute(AuthHelper.AUTH_USER_PROPERTY);
        if (authUser == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        return Response.ok(authUser).build();
    }

    @Path("/nextUsername")
    @GET
    @Filters.InitialUserAuthRequired
    public Response getNextUsername(@QueryParam("deviceId") String deviceId) throws SQLException {
        try (Connection dbConn = wordDbManager.getConnection()) {

            if (!Strings.isNullOrEmpty(deviceId)) {
                Optional<UserModel> existingUser = usersDAO.getUserByDeviceId(dbConn, deviceId);
                if (existingUser.isPresent()) {
                    return Response.ok(new NextUsernameModel(existingUser.get().getUsername(), true))
                            .build();
                }
            }
            Optional<String> randomUsername = RandomUsernamePicker.getInstance().generateRandomUsername(dbConn);
            String nextUsername = randomUsername.isPresent() ? randomUsername.get() : null;
            return Response.ok(new NextUsernameModel(nextUsername, false)).build();
        }
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
        if (maxResults <= 0 || maxResults > MAX_COUNT) {
            return Response.status(BAD_REQUEST)
                    .entity(new ErrorModel("Must provide the query param \"maxResults\" and it must be > 0 and <= " + MAX_COUNT))
                    .build();
        }
        try (Connection dbConn = wordDbManager.getConnection()) {
            List<UserModel> results = usersProvider.searchUsers(dbConn, q, maxResults);
            return Response.ok(new UserListModel(results)).build();
        }
    }

    @Path("/similarRating")
    @GET
    @Filters.RegularUserAuthRequired
    public Response getUsersWithSimilarRating(@Context HttpServletRequest request, @QueryParam("maxResults") int maxResults)
            throws Exception {
        UserModel authUser = (UserModel) request.getAttribute(AuthHelper.AUTH_USER_PROPERTY);
        if (authUser == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        if (maxResults <= 0 || maxResults > MAX_COUNT) {
            return Response.status(BAD_REQUEST)
                    .entity(new ErrorModel("Must provide the query param \"maxResults\" and it must be > 0 and <= " + MAX_COUNT))
                    .build();
        }
        try (Connection dbConn = wordDbManager.getConnection()) {
            List<UserModel> results = ratingsProvider.getUsersWithRatingsAroundMe(dbConn, authUser, maxResults);
            return Response.ok(new UserListModel(results)).build();
        }
    }

    @Path("/{id}/similarRank")
    @GET
    @Filters.RegularUserAuthRequired
    public Response getUsersWithSimilarRank(@Context HttpServletRequest request,
                                            @PathParam("id") int userId,
                                            @QueryParam("maxResults") int maxResults)
            throws Exception {
        UserModel authUser = (UserModel) request.getAttribute(AuthHelper.AUTH_USER_PROPERTY);
        if (authUser == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        if (maxResults <= 0 || maxResults > MAX_COUNT) {
            return Response.status(BAD_REQUEST)
                    .entity(new ErrorModel("Must provide the query param \"maxResults\" and it must be > 0 and <= " + MAX_COUNT))
                    .build();
        }
        try (Connection dbConn = wordDbManager.getConnection()) {
            List<UserModel> results = ratingsProvider.getUsersWithRankAroundMe(dbConn, authUser, maxResults);
            return Response.ok(new UserListModel(results)).build();
        }
    }

    @Path("/bestRanked")
    @GET
    @Filters.RegularUserAuthRequired
    public Response getBestRankedUsers(@Context HttpServletRequest request,
                                       @QueryParam("maxResults") int maxResults)
            throws Exception {
        if (maxResults <= 0 || maxResults > MAX_COUNT) {
            return Response.status(BAD_REQUEST)
                    .entity(new ErrorModel("Must provide the query param \"maxResults\" and it must be > 0 and <= " + MAX_COUNT))
                    .build();
        }
        try (Connection dbConn = wordDbManager.getConnection()) {
            List<UserModel> results = usersDAO.getUsersWithBestRanks(dbConn, maxResults);
            return Response.ok(new UserListModel(results)).build();
        }
    }

    @Path("/professorRank")
    @GET
    @Filters.RegularUserAuthRequired
    public Response getRanksAroundProfessor(@Context HttpServletRequest request,
                                            @QueryParam("maxResults") int maxResults)
            throws Exception {
        if (maxResults <= 0 || maxResults > MAX_COUNT) {
            return Response.status(BAD_REQUEST)
                    .entity(new ErrorModel("Must provide the query param \"maxResults\" and it must be > 0 and <= " + MAX_COUNT))
                    .build();
        }
        try (Connection dbConn = wordDbManager.getConnection()) {
            List<UserModel> results = ratingsProvider.getUsersWithRankAroundMe(dbConn, WordConstants.PROFESSOR_AI_USER.get(), maxResults);
            return Response.ok(new UserListModel(results)).build();
        }
    }

    @Path("/bookwormRank")
    @GET
    @Filters.RegularUserAuthRequired
    public Response getRanksAroundBookworm(@Context HttpServletRequest request,
                                            @QueryParam("maxResults") int maxResults)
            throws Exception {
        if (maxResults <= 0 || maxResults > MAX_COUNT) {
            return Response.status(BAD_REQUEST)
                    .entity(new ErrorModel("Must provide the query param \"maxResults\" and it must be > 0 and <= " + MAX_COUNT))
                    .build();
        }
        try (Connection dbConn = wordDbManager.getConnection()) {
            List<UserModel> results = ratingsProvider.getUsersWithRankAroundMe(dbConn, WordConstants.BOOKWORM_AI_USER.get(), maxResults);
            return Response.ok(new UserListModel(results)).build();
        }
    }

    @Path("/monkeyRank")
    @GET
    @Filters.RegularUserAuthRequired
    public Response getRanksAroundMonkey(@Context HttpServletRequest request,
                                           @QueryParam("maxResults") int maxResults)
            throws Exception {
        if (maxResults <= 0 || maxResults > MAX_COUNT) {
            return Response.status(BAD_REQUEST)
                    .entity(new ErrorModel("Must provide the query param \"maxResults\" and it must be > 0 and <= " + MAX_COUNT))
                    .build();
        }
        try (Connection dbConn = wordDbManager.getConnection()) {
            List<UserModel> results = ratingsProvider.getUsersWithRankAroundMe(dbConn, WordConstants.RANDOM_AI_USER.get(), maxResults);
            return Response.ok(new UserListModel(results)).build();
        }
    }

    @Path("/bestMatch")
    @GET
    @Filters.RegularUserAuthRequired
    public Response getMyBestMatch(@Context HttpServletRequest request)
            throws Exception {
        UserModel authUser = (UserModel) request.getAttribute(AuthHelper.AUTH_USER_PROPERTY);
        if (authUser == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        try (Connection dbConn = wordDbManager.getConnection()) {
            UserModel bestMatch = ratingsProvider.getBestMatch(dbConn, authUser);
            return Response.ok(bestMatch).build();
        }
    }

    @Path("myGamesSummary")
    @GET
    @Filters.RegularUserAuthRequired
    public Response getMainMenuInfos(@Context HttpServletRequest request) throws SQLException {
        UserModel authUser = (UserModel) request.getAttribute(AuthHelper.AUTH_USER_PROPERTY);
        if (authUser == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        try(Connection dbConn = wordDbManager.getConnection()) {
            UserGameSummaryModel userGameSummaryModel = usersProvider.getUserSummaryInfoForMainMenu(authUser.getId(), dbConn);
            return Response.ok(userGameSummaryModel).build();
        }
    }

    @Path("setPassword")
    @POST
    @Filters.RegularUserAuthRequired
    public Response setPassword(@Context HttpServletRequest request, @QueryParam("pass") String pass) throws Exception {
        UserModel authUser = (UserModel) request.getAttribute(AuthHelper.AUTH_USER_PROPERTY);
        if (authUser == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<ErrorModel> errorOpt = usersProvider.isValidPassword(pass);
        if (errorOpt.isPresent()) {
            return Response.status(BAD_REQUEST)
                    .entity(errorOpt.get())
                    .build();
        }

        try (Connection dbConn = wordDbManager.getConnection()) {
            errorOpt = usersProvider.canUpdatePassword(authUser.getId(), dbConn);
            if (errorOpt.isPresent()) {
                return Response.status(BAD_REQUEST)
                        .entity(errorOpt.get())
                        .build();
            }

            usersProvider.updateUserPassword(authUser.getId(), pass, dbConn);
        }

        return Response.ok(new GenericOkModel("Password set successfully")).build();
    }

    // ------ Helpers ----
    public URI getWordUserURI(int id) {
        return uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(id))
                .build();
    }


}
