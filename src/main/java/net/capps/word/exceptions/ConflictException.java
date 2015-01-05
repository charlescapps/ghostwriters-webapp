package net.capps.word.exceptions;

/**
 * Created by charlescapps on 12/28/14.
 */
public class ConflictException extends WordDbException {
    private String field;

    public ConflictException(String field, String message) {
        super(message);
        this.field = field;
    }

    public ConflictException(String field, String message, Exception cause) {
        super(message, cause);
        this.field = field;
    }
}
