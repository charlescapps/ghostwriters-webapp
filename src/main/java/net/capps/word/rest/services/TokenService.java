package net.capps.word.rest.services;

import net.capps.word.rest.providers.TokensProvider;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by charlescapps on 5/12/15.
 */
@Path(TokenService.TOKEN_PATH)
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
public class TokenService {
    public static final String TOKEN_PATH = "tokens";
    private static final TokensProvider tokensProvider = TokensProvider.getInstance();

}
