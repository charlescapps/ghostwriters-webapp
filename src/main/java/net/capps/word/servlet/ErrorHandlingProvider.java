package net.capps.word.servlet;

import net.capps.word.rest.models.ErrorModel;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
 public class ErrorHandlingProvider implements ExceptionMapper<Exception>
{

    @Override
    public Response toResponse(Exception exception)
    {
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity( new ErrorModel(exception.getMessage()))
                .type( MediaType.APPLICATION_JSON_TYPE)
                .build();
    }

}