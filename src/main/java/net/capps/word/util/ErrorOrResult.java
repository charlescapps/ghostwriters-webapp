package net.capps.word.util;

import com.google.common.base.Preconditions;
import net.capps.word.rest.models.ErrorModel;

import java.util.Optional;

/**
 * Created by charlescapps on 3/26/15.
 */
public class ErrorOrResult<T> {
    private ErrorModel error;
    private T result;

    private ErrorOrResult(ErrorModel error, T result) {
        this.error = error;
        this.result = result;
    }

    public static <T> ErrorOrResult<T> ofError(ErrorModel error) {
        Preconditions.checkNotNull(error);
        return new ErrorOrResult<>(error, null);
    }

    public static <T> ErrorOrResult<T> ofResult(T result) {
        Preconditions.checkNotNull(result);
        return new ErrorOrResult<>(null, result);
    }

    public Optional<ErrorModel> getError() {
        return Optional.ofNullable(error);
    }

    public Optional<T> getResult() {
        return Optional.ofNullable(result);
    }
}
