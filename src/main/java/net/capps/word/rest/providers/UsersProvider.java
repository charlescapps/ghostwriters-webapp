package net.capps.word.rest.providers;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import net.capps.word.db.dao.UsersDAO;
import net.capps.word.models.ErrorModel;
import net.capps.word.models.UserModel;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by charlescapps on 12/27/14.
 */
public class UsersProvider {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9_ \\-]*");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("[a-zA-Z0-9!@#$%^&*\\(\\)\\-_=\\+\\[\\]\\{\\}]+");
    private static final int MIN_USERNAME_LEN = 3;
    private static final int MAX_USERNAME_LEN = 16;

    private static final int MIN_PASSWORD_LEN = 4;
    private static final int MAX_PASSWORD_LEN = 32;

    private static final int SALT_BYTES = 8;

    private static final UsersDAO USERS_DAO = new UsersDAO();

    //------------- Public --------------
    public Optional<ErrorModel> validateInputUser(UserModel userModel) {
        if (Strings.isNullOrEmpty(userModel.getUsername())) {
            return Optional.of(new ErrorModel("Missing username"));
        }
        if (Strings.isNullOrEmpty(userModel.getPassword())) {
            return Optional.of(new ErrorModel("Missing password"));
        }
        Optional<ErrorModel> usernameError = isValidUsername(userModel.getUsername());
        if (usernameError.isPresent()) {
            return usernameError;
        }

        return isValidPassword(userModel.getPassword());
    }

    public UserModel createNewUser(UserModel validatedInput) throws Exception {
        byte[] salt = generateSalt();
        byte[] hashPass = hashPassUsingSha256(validatedInput.getPassword(), salt);
        return USERS_DAO.insertNewUser(validatedInput, byteToBase64(hashPass), byteToBase64(salt));
    }

    public Optional<UserModel> getUserById(int id) throws Exception {
        return USERS_DAO.getUserById(id);
    }

    //------------- Private ------------

    //------- Crypto helpers -----------
    private byte[] hashPassUsingSha256(String validatedPassword, byte[] salt)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(salt);
        return digest.digest(validatedPassword.getBytes("UTF-8"));
    }

    private byte[] generateSalt() {
        Random random = new Random();
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * From a base 64 representation, returns the corresponding byte[]
     * @param data String The base64 representation
     * @return byte[]
     * @throws IOException
     */
    private static byte[] base64ToByte(String data) throws IOException {
        BASE64Decoder decoder = new BASE64Decoder();
        return decoder.decodeBuffer(data);
    }

    /**
     * From a byte[] returns a base 64 representation
     * @param data byte[]
     * @return String
     * @throws IOException
     */
    private static String byteToBase64(byte[] data){
        BASE64Encoder endecoder = new BASE64Encoder();
        return endecoder.encode(data);
    }

    //------- Validation helpers -------
    private Optional<ErrorModel> isValidUsername(String username) {
        if (username.length() < MIN_USERNAME_LEN || username.length() > MAX_USERNAME_LEN) {
            return Optional.of(new ErrorModel(
                    String.format("Username must be between %d and %d characters in length.", MIN_USERNAME_LEN, MAX_USERNAME_LEN)));
        }
        Matcher m = USERNAME_PATTERN.matcher(username);
        if (!m.matches()) {
            return Optional.of(new ErrorModel("Username must start with an alphanumeric character and can have letters, numbers, spaces, '_', and '-'"));
        }
        return Optional.absent();
    }

    private Optional<ErrorModel> isValidPassword(String password) {
        if (password.length() < MIN_PASSWORD_LEN || password.length() > MAX_PASSWORD_LEN) {
            return Optional.of(new ErrorModel(
                    String.format("Password length must be between %d and %d characters", MIN_PASSWORD_LEN, MAX_PASSWORD_LEN)
            ));
        }
        Matcher m = PASSWORD_PATTERN.matcher(password);
        if (!m.matches()) {
            return Optional.of(new ErrorModel("Password can only contain letters, numbers, or !,@,#,$,%,^,&,*,(,),-,_,+,=,{,},[,]"));
        }
        return Optional.absent();
    }
}
