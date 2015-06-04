package net.capps.word.rest.services;

import net.capps.word.game.dict.SpecialDict;
import net.capps.word.rest.auth.AuthHelper;
import net.capps.word.rest.filters.Filters;
import net.capps.word.rest.models.DictionaryModel;
import net.capps.word.rest.models.ErrorModel;
import net.capps.word.rest.models.UserModel;
import net.capps.word.rest.models.WordModel;
import net.capps.word.rest.providers.DictionaryProvider;
import net.capps.word.rest.providers.PlayedWordsProvider;
import net.capps.word.util.ErrorOrResult;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by charlescapps on 6/3/15.
 */
@Path("dictionary")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Filters.RegularUserAuthRequired
public class DictionaryService {
    private static final DictionaryProvider dictionaryProvider = DictionaryProvider.getInstance();
    private static final PlayedWordsProvider playedWordsProvider = PlayedWordsProvider.getInstance();

    @GET
    @Path("{dictName}")
    public Response getDictionary(@Context HttpServletRequest request, @PathParam("dictName") String dictName)
            throws SQLException {
        UserModel authUser = (UserModel) request.getAttribute(AuthHelper.AUTH_USER_PROPERTY);
        if (authUser == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorModel("You must login to perform this action."))
                    .build();
        }

        ErrorOrResult<SpecialDict> errorOrResult = dictionaryProvider.validateSpecialDictionary(dictName);
        if (errorOrResult.isError()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorOrResult.getError().get())
                    .build();
        }
        SpecialDict specialDict = errorOrResult.getResult().get();

        List<WordModel> sortedWords = playedWordsProvider.getPlayedWordsForSpecialDict(authUser.getId(), specialDict);
        DictionaryModel dictionaryModel = new DictionaryModel(specialDict, sortedWords);

        return Response.ok(dictionaryModel).build();
    }
}
