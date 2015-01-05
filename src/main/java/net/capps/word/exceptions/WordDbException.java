package net.capps.word.exceptions;

/**
 * Created by charlescapps on 12/28/14.
 */
public class WordDbException extends Exception {
    public WordDbException(String message) {
        super(message);
    }

    public WordDbException(String message, Exception cause) {
        super(message, cause);
    }
}
