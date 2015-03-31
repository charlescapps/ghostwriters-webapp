package net.capps.word.servlet;

import com.google.common.base.Throwables;
import net.capps.word.rest.models.ErrorModel;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
 public class ErrorHandlingProvider implements ExceptionMapper<Throwable>
{

    @Override
    public Response toResponse(Throwable exception)
    {
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(new ErrorModel(Throwables.getStackTraceAsString(exception)))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

}