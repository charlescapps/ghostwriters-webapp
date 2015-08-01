package net.capps.word.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

/**
 * Created by charlescapps on 8/1/15.
 */
public class DbIterator<T> implements Iterator<T> {
    private final ResultSet resultSet;
    private final Function<ResultSet, T> func;

    public DbIterator(ResultSet resultSet, Function<ResultSet, T> func) {
        this.resultSet = resultSet;
        this.func = func;
    }

    @Override
    public boolean hasNext() {
        try {
            return !resultSet.isAfterLast();
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException("The result set is after the last row!");
        }
        try {
            if (!resultSet.next()) {
                throw new NoSuchElementException("resultSet.next() returned false!");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("SQLException getting the next row:", e);
        }

        return func.apply(resultSet);
    }
}
