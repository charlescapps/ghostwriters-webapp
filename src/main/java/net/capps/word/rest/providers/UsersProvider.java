package net.capps.word.rest.providers;

import com.google.common.base.Strings;
import net.capps.word.crypto.CryptoUtils;
import net.capps.word.db.dao.UsersDAO;
import net.capps.word.rest.models.ErrorModel;
import net.capps.word.rest.models.UserGameSummaryModel;
import net.capps.word.rest.models.UserModel;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

/**
 * Created by charlescapps on 12/27/14.
 */
public class UsersProvider {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9_ \\-]*");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("[a-zA-Z0-9!@#$%^&*\\(\\)\\-_=\\+\\[\\]\\{\\}]+");
    public static final int MIN_USERNAME_LEN = 4;
    public static final int MAX_USERNAME_LEN = 16;

    private static final int MIN_PASSWORD_LEN = 4;
    private static final int MAX_PASSWORD_LEN = 20;

    private static final int SALT_BYTES = 8;

    private static final UsersDAO usersDao = UsersDAO.getInstance();

    private static final UsersProvider INSTANCE = new UsersProvider();

    // -------- ErrorModels -------
    private static final ErrorModel ERR_PASSWORD_ALREADY_SET = new ErrorModel("You have already set a password.");

    private UsersProvider() { }

    public static UsersProvider getInstance() {
        return INSTANCE;
    }

    //------------- Public --------------
    public Optional<ErrorModel> validateInputUser(UserModel userModel) {
        String inputUsername = userModel.getUsername();
        if (Strings.isNullOrEmpty(inputUsername)) {
            return Optional.of(new ErrorModel("Missing username"));
        }
        userModel.setUsername(preProcessUsername(inputUsername));
        if (Strings.isNullOrEmpty(userModel.getDeviceId())) {
            return Optional.of(new ErrorModel("Missing device ID"));
        }
        Optional<ErrorModel> usernameError = isValidUsername(userModel.getUsername());
        if (usernameError.isPresent()) {
            return usernameError;
        }
        // Validate the email, if present.
        if (userModel.getEmail() != null) {
            userModel.setEmail(userModel.getEmail().trim());
            if (userModel.getEmail().isEmpty()) {
                userModel.setEmail(null); // If the client sends the empty string, use null
            } else {
                try {
                    InternetAddress emailAddress = new InternetAddress(userModel.getEmail());
                    emailAddress.validate();
                } catch (AddressException e) {
                    return Optional.of(new ErrorModel(
                            format("The given email address '%s' is not a valid email address.", userModel.getEmail())));
                }
            }
        }

        return Optional.empty();
    }

    public UserModel createNewUser(UserModel validatedInput) throws Exception {
        return usersDao.insertNewUser(validatedInput);
    }

    public void updateUserPassword(int userId, String validatedPass, Connection dbConn) throws Exception {
        byte[] salt = generateSalt();
        byte[] hashPass = hashPassUsingSha256(validatedPass, salt);
        usersDao.updateUserPassword(dbConn, userId, CryptoUtils.byteToBase64(hashPass), CryptoUtils.byteToBase64(salt));
    }

    public Optional<UserModel> createNewUserIfNotExists(UserModel validatedInput) throws Exception {
        Optional<UserModel> existing = usersDao.getUserByUsername(validatedInput.getUsername(), false);
        if (existing.isPresent()) {
            return Optional.empty();
        }
        if (validatedInput.getEmail() != null) {
            Optional<UserModel> existingEmail = usersDao.getUserByEmail(validatedInput.getEmail());
            if (existingEmail.isPresent()) {
                return Optional.empty();
            }
        }
        return Optional.of(createNewUser(validatedInput));
    }

    public List<UserModel> searchUsers(String q, int maxResults) throws Exception {
        List<UserModel> results = new ArrayList<>();
        // First add the exact match (case insensitive) to the beginning of the list.
        Optional<UserModel> exactMatch = usersDao.getUserByUsername(q, false);
        if (exactMatch.isPresent()) {
            results.add(exactMatch.get());
        }

        if (results.size() >= maxResults) {
            return results;
        }

        // Next add prefix matches
        List<UserModel> prefixMatches = usersDao.searchUsers(q, UsersDAO.SearchType.PREFIX, maxResults);
        prefixMatches.removeAll(results); // Eliminate duplicates
        results.addAll(prefixMatches);

        if (results.size() >= maxResults) {
            return results.subList(0, maxResults);
        }

        // Next add substring matches
        List<UserModel> substringMatches = usersDao.searchUsers(q, UsersDAO.SearchType.SUBSTRING, maxResults);
        substringMatches.removeAll(results); // Eliminate duplicates
        results.addAll(substringMatches);

        return results.subList(0, Math.min(maxResults, results.size()));
    }

    public UserGameSummaryModel getUserSummaryInfoForMainMenu(int userId, Connection dbConn) throws SQLException {
        final int numGamesMyTurn = usersDao.getNumGamesMyTurn(userId, dbConn);
        final int numGamesOfferedToMe = usersDao.getNumGamesOfferedToMe(userId, dbConn);

        return new UserGameSummaryModel(userId, numGamesMyTurn, numGamesOfferedToMe);
    }

    //------------- Private ------------

    //------- Crypto helpers -----------
    public static byte[] hashPassUsingSha256(String validatedPassword, byte[] salt)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(salt);
        return digest.digest(validatedPassword.getBytes("UTF-8"));
    }

    private byte[] generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        ThreadLocalRandom.current().nextBytes(salt);
        return salt;
    }

    //------- Validation helpers -------
    private Optional<ErrorModel> isValidUsername(String username) {
        if (username.length() < MIN_USERNAME_LEN || username.length() > MAX_USERNAME_LEN) {
            return Optional.of(new ErrorModel(
                    format("Username must be between %d and %d characters in length.", MIN_USERNAME_LEN, MAX_USERNAME_LEN)));
        }
        Matcher m = USERNAME_PATTERN.matcher(username);
        if (!m.matches()) {
            return Optional.of(new ErrorModel("Username can only use letters, numbers, spaces, -, or _."));
        }
        if (username.contains("  ")) {
            return Optional.of(new ErrorModel("Username can't have 2 spaces in a row."));
        }
        return Optional.empty();
    }

    private String preProcessUsername(String username) {
        username = username.trim();
        return username.replaceAll("  +", " ");
    }

    public Optional<ErrorModel> isValidPassword(String password) {
        if (password.length() < MIN_PASSWORD_LEN || password.length() > MAX_PASSWORD_LEN) {
            return Optional.of(new ErrorModel(
                    format("Password length must be between %d and %d characters", MIN_PASSWORD_LEN, MAX_PASSWORD_LEN)
            ));
        }
        Matcher m = PASSWORD_PATTERN.matcher(password);
        if (!m.matches()) {
            return Optional.of(new ErrorModel("Password can contain letters, numbers, or !,@,#,$,%,^,&,*,(,),-,_,+,=,{,},[,]"));
        }
        return Optional.empty();
    }

    public Optional<ErrorModel> canUpdatePassword(int userId, Connection dbConn) throws SQLException {
        boolean isPasswordDefined = usersDao.isUserPasswordDefined(dbConn, userId);
        if (isPasswordDefined) {
            return Optional.of(ERR_PASSWORD_ALREADY_SET);
        }
        return Optional.empty();
    }


}
