package net.capps.word.util;

import com.google.common.base.Preconditions;

/**
 * Created by charlescapps on 3/31/15.
 */
public class SingletonHolder<T> {
    private T value;

    private SingletonHolder(T value) {
        this.value = value;
    }

    public static <T> SingletonHolder<T> of(T value) {
        return new SingletonHolder<>(Preconditions.checkNotNull(value));
    }

    public static <T> SingletonHolder<T> absent() {
        return new SingletonHolder<>(null);
    }

    public boolean isPresent() {
        return value != null;
    }

    public void set(T value) {
        if (this.value == null) {
            this.value = Preconditions.checkNotNull(value);
        } else {
            throw new IllegalStateException("SingletonHolder can only be set exactly once!");
        }
    }

    public T get() {
        if (value == null) {
            throw new IllegalStateException("Cannot get the value of a SingletonHolder that isn't present!");
        }
        return value;
    }
}
