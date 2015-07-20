package net.capps.word.util;

import net.capps.word.rest.models.ErrorModel;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status;

/**
 * Created by charlescapps on 7/19/15.
 */
public class RestUtil {
    public static Response badRequest(ErrorModel errorModel) {
        return Response.status(Status.BAD_REQUEST)
                .entity(errorModel)
                .build();
    }
}
