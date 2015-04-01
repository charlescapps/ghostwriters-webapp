package net.capps.word.rest.auth;

import com.google.common.base.Optional;
import com.google.common.net.HttpHeaders;
import net.capps.word.constants.WordConstants;
import net.capps.word.crypto.CryptoUtils;
import net.capps.word.db.dao.SessionsDAO;
import net.capps.word.db.dao.UserHashInfo;
import net.capps.word.db.dao.UsersDAO;
import net.capps.word.exceptions.AuthError;
import net.capps.word.exceptions.WordAuthException;
import net.capps.word.rest.models.SessionModel;
import net.capps.word.rest.models.UserModel;
import net.capps.word.rest.providers.SessionProvider;
import net.capps.word.rest.providers.UsersProvider;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by charlescapps on 12/28/14.
 */
public class AuthHelper {
    public static final String COOKIE_NAME = "WORDS_SESSIONID";
    public static final String AUTH_USER_PROPERTY = "WORDS_AUTH_USER";

    private static final AuthHelper INSTANCE = new AuthHelper();
    private static final Pattern BASIC_AUTH = Pattern.compile("Basic ([a-zA-Z0-9=]+)");
    private static final SessionProvider sessionProvider = SessionProvider.getInstance();
    private static final SessionsDAO sessionsDao = SessionsDAO.getInstance();
    private static final UsersDAO usersDAO = UsersDAO.getInstance();

    private static final String MISSING_AUTHZ_HEADER_MSG = "Missing Authorization header";
    private static final String INVALID_AUTHZ_HEADER_MSG = "Invalid Basic Auth header";
    private static final String INVALID_USERNAME_OR_PASS_MSG = "Invalid username or password";

    private AuthHelper() {}

    public static AuthHelper getInstance() {
        return INSTANCE;
    }

    public Pair<UserModel, SessionModel> loginUsingBasicAuth(HttpServletRequest request) throws Exception {

        UserModel authenticatedUser = getUserForBasicAuth(request);

        if (WordConstants.INITIAL_USER_USERNAME.equals(authenticatedUser.getUsername())) {
            throw new WordAuthException(INVALID_USERNAME_OR_PASS_MSG, AuthError.MISSING_USERNAME);
        }

        // Create a new session
        SessionModel session = sessionProvider.createNewSession(authenticatedUser);

        return ImmutablePair.of(authenticatedUser, session);
    }

    /**
     * Get the user for the given credentials.
     *
     * @param request the request
     * @return the authenticated user.
     * @throws net.capps.word.exceptions.WordAuthException - if the credentials are invalid.
     */
    public UserModel getUserForBasicAuth(HttpServletRequest request) throws Exception {

        String basicAuthHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (basicAuthHeader == null) {
            throw new WordAuthException(MISSING_AUTHZ_HEADER_MSG, AuthError.INVALID_BASIC_AUTH);
        }

        Matcher m = BASIC_AUTH.matcher(basicAuthHeader);
        if (!m.matches()) {
            throw new WordAuthException(INVALID_AUTHZ_HEADER_MSG, AuthError.INVALID_BASIC_AUTH);
        }

        String base64Part = m.group(1);

        String decoded = new String(CryptoUtils.base64ToByte(base64Part));

        String[] tokens = decoded.split(":");
        if (tokens.length != 2) {
            throw new WordAuthException(INVALID_AUTHZ_HEADER_MSG, AuthError.INVALID_BASIC_AUTH);
        }
        String username = tokens[0];
        String password = tokens[1];

        return authenticate(username, password);
    }

    /**
     * Get the user for the given credentials.
     *
     * @param username
     * @param password
     * @return the authenticated user.
     * @throws net.capps.word.exceptions.WordAuthException - if the credentials are invalid.
     */
    public UserModel authenticate(String username, String password) throws Exception {
        Optional<UserModel> user = UsersDAO.getInstance().getUserByUsername(username, false);
        if (!user.isPresent()) {
            throw new WordAuthException(INVALID_USERNAME_OR_PASS_MSG, AuthError.MISSING_USERNAME);
        }

        UserHashInfo hashInfo = user.get().getUserHashInfo();
        byte[] salt = CryptoUtils.base64ToByte(hashInfo.getSalt());
        byte[] hashInputPass = UsersProvider.hashPassUsingSha256(password, salt);
        byte[] storedHashPass = CryptoUtils.base64ToByte(hashInfo.getHashPass());

        if (!Arrays.equals(hashInputPass, storedHashPass)) {
            throw new WordAuthException(INVALID_USERNAME_OR_PASS_MSG, AuthError.INVALID_PASSWORD);
        }

        return user.get();

    }

    public Optional<UserModel> validateSession(HttpServletRequest request) throws Exception {
        Cookie wordsCookie = null;
        for (Cookie cookie: request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                wordsCookie = cookie;
            }
        }
        if (wordsCookie == null) {
            return Optional.absent();
        }

        String sessionId = wordsCookie.getValue();
        Optional<SessionModel> session = sessionsDao.getSessionForSessionId(sessionId);
        if (!session.isPresent()) {
            return Optional.absent();
        }
        int userId = session.get().getUserId();
        return usersDAO.getUserById(userId);
    }
}
