package net.capps.word.rest.auth;

import com.google.common.base.Optional;
import com.google.common.net.HttpHeaders;
import net.capps.word.crypto.CryptoUtils;
import net.capps.word.db.dao.UserHashInfo;
import net.capps.word.db.dao.UsersDAO;
import net.capps.word.exceptions.AuthError;
import net.capps.word.exceptions.WordAuthException;
import net.capps.word.rest.models.UserModel;
import net.capps.word.rest.providers.UsersProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by charlescapps on 12/28/14.
 */
public class AuthHelper {
    private static final AuthHelper INSTANCE = new AuthHelper();

    private static final Pattern BASIC_AUTH = Pattern.compile("Basic ([a-zA-Z0-9=]+)");
    private static final String SESSION_USER_ID = "word_attack_user";

    private AuthHelper() {}

    public static AuthHelper getInstance() {
        return INSTANCE;
    }

    public UserModel loginUsingBasicAuth(HttpServletRequest request) throws Exception {

        String basicAuthHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (basicAuthHeader == null) {
            throw new WordAuthException("Authorization header must be provided.", AuthError.INVALID_BASIC_AUTH);
        }

        Matcher m = BASIC_AUTH.matcher(basicAuthHeader);
        if (!m.matches()) {
            throw new WordAuthException("Invalid Authorization header.", AuthError.INVALID_BASIC_AUTH);
        }

        String base64Part = m.group(1);

        String decoded = new String(CryptoUtils.base64ToByte(base64Part));

        String[] tokens = decoded.split(":");
        if (tokens.length != 2) {
            throw new WordAuthException("Invalid Authorization header", AuthError.INVALID_BASIC_AUTH);
        }
        String username = tokens[0];
        String password = tokens[1];

        UserModel authenticatedUser = authenticate(username, password);

        // Create a new servlet session
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_USER_ID, authenticatedUser.getId());

        return authenticatedUser;
    }

    public UserModel authenticate(String username, String password) throws Exception {
        Optional<UserModel> user = UsersDAO.getInstance().getUserByUsername(username);
        if (!user.isPresent()) {
            throw new WordAuthException("Invalid username or password", AuthError.MISSING_USERNAME);
        }

        UserHashInfo hashInfo = user.get().getUserHashInfo();
        byte[] salt = CryptoUtils.base64ToByte(hashInfo.getSalt());
        byte[] hashInputPass = UsersProvider.hashPassUsingSha256(password, salt);
        byte[] storedHashPass = CryptoUtils.base64ToByte(hashInfo.getHashPass());

        if (!Arrays.equals(hashInputPass, storedHashPass)) {
            throw new WordAuthException("Invalid username or password", AuthError.INVALID_PASSWORD);
        }

        return user.get();

    }

    public Optional<UserModel> validateSession(HttpServletRequest request) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SESSION_USER_ID) == null) {
            return Optional.absent();
        }
        int userId = (Integer) session.getAttribute(SESSION_USER_ID);
        return UsersDAO.getInstance().getUserById(userId);
    }

    public Optional<UserModel> validateSessionUser(HttpServletRequest request, String username) throws Exception {
        Optional<UserModel> user = validateSession(request);
        if (!user.isPresent() ||
            !username.equals(user.get().getUsername())) {
            return Optional.absent();
        }
        return user;
    }
}
