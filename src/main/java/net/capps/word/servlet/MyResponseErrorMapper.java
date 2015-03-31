package net.capps.word.servlet;

import com.google.common.base.Throwables;
import net.capps.word.rest.models.ErrorModel;
import org.glassfish.jersey.server.spi.ResponseErrorMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
 public class MyResponseErrorMapper implements ResponseErrorMapper {
    private static final Logger LOG = LoggerFactory.getLogger(MyResponseErrorMapper.class);

    @Override
    public Response toResponse(Throwable exception)
    {
        LOG.error("ERROR: {}", Throwables.getStackTraceAsString(exception));
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(new ErrorModel(Throwables.getStackTraceAsString(exception)))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

}