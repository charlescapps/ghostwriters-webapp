package net.capps.word.rest.auth;

import com.google.common.base.Strings;
import com.google.common.net.HttpHeaders;
import net.capps.word.constants.WordConstants;
import net.capps.word.crypto.CryptoUtils;
import net.capps.word.db.WordDbManager;
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
import java.sql.Connection;
import java.util.Arrays;
import java.util.Optional;
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
    private static final String PASSWORD_NOT_DEFINED = "User hasn't set a password";

    private AuthHelper() {
    }

    public static AuthHelper getInstance() {
        return INSTANCE;
    }

    public Pair<UserModel, SessionModel> loginUsingBasicAuth(HttpServletRequest request) throws Exception {

        // Create a new session
        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            UserModel authenticatedUser = getUserForBasicAuth(dbConn, request);

            if (WordConstants.INITIAL_USER_USERNAME.equals(authenticatedUser.getUsername())) {
                throw new WordAuthException(INVALID_USERNAME_OR_PASS_MSG, AuthError.MISSING_USERNAME);
            }

            SessionModel session = sessionProvider.createNewSession(dbConn, authenticatedUser);
            return ImmutablePair.of(authenticatedUser, session);
        }
    }

    /**
     * Get the user for the given credentials.
     *
     * @param request the request
     * @return the authenticated user.
     * @throws net.capps.word.exceptions.WordAuthException - if the credentials are invalid.
     */
    public UserModel getUserForBasicAuth(Connection dbConn, HttpServletRequest request) throws Exception {

        Pair<String, String> usernamePass = getUsernamePassFromAuthzHeader(request);

        return authenticate(dbConn, usernamePass.getLeft(), usernamePass.getRight());
    }

    public Pair<String, String> getUsernamePassFromAuthzHeader(HttpServletRequest request) throws Exception {
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

        return ImmutablePair.of(tokens[0], tokens[1]);
    }

    /**
     * Get the user for the given credentials.
     *
     * @param username
     * @param password
     * @return the authenticated user.
     * @throws net.capps.word.exceptions.WordAuthException - if the credentials are invalid.
     */
    public UserModel authenticate(Connection dbConn, String username, String password) throws Exception {
        Optional<UserModel> user = UsersDAO.getInstance().getUserByUsername(dbConn, username, false);
        if (!user.isPresent()) {
            throw new WordAuthException(INVALID_USERNAME_OR_PASS_MSG, AuthError.MISSING_USERNAME);
        }

        UserHashInfo hashInfo = user.get().getUserHashInfo();

        // If the UserHashInfo doesn't have a hash pass & a salt, this means the user hasn't defined a password yet.
        if (!isValidUserHashInfo(hashInfo)) {
            throw new WordAuthException(PASSWORD_NOT_DEFINED, AuthError.INVALID_PASSWORD);
        }

        byte[] salt = CryptoUtils.base64ToByte(hashInfo.getSalt());
        byte[] hashInputPass = UsersProvider.hashPassUsingSha256(password, salt);
        byte[] storedHashPass = CryptoUtils.base64ToByte(hashInfo.getHashPass());

        if (!Arrays.equals(hashInputPass, storedHashPass)) {
            throw new WordAuthException(INVALID_USERNAME_OR_PASS_MSG, AuthError.INVALID_PASSWORD);
        }

        return user.get();

    }

    public Optional<UserModel> validateSession(Connection dbConn, HttpServletRequest request) throws Exception {
        Cookie wordsCookie = null;
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                wordsCookie = cookie;
            }
        }
        if (wordsCookie == null) {
            return Optional.empty();
        }

        String sessionId = wordsCookie.getValue();
        Optional<SessionModel> session = sessionsDao.getSessionForSessionId(dbConn, sessionId);
        if (!session.isPresent()) {
            return Optional.empty();
        }
        int userId = session.get().getUserId();
        return usersDAO.getUserById(dbConn, userId);
    }

    private boolean isValidUserHashInfo(UserHashInfo userHashInfo) {
        return userHashInfo != null &&
                !Strings.isNullOrEmpty(userHashInfo.getHashPass()) &&
                !Strings.isNullOrEmpty(userHashInfo.getSalt());
    }
}
