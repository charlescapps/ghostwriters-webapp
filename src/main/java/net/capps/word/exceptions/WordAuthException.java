package net.capps.word.exceptions;

/**
 * Created by charlescapps on 12/28/14.
 */
public class WordAuthException extends Exception {
    private final AuthError authError;

    public WordAuthException(String message, AuthError authError) {
        super(message);
        this.authError = authError;
    }

    public AuthError getAuthError() {
        return authError;
    }
}
