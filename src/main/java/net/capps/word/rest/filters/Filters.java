package net.capps.word.rest.filters;

import javax.ws.rs.NameBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by charlescapps on 1/19/15.
 *
 * Annotations for Jersey filters.
 */
public class Filters {

    @NameBinding
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface RegularUserAuthRequired {}

    @NameBinding
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface InitialUserAuthRequired {}

}
