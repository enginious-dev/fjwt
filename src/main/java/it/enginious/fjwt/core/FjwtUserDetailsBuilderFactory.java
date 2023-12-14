package it.enginious.fjwt.core;

import java.util.function.Function;

/**
 * The factory that creates all the instances of the {@link FjwtAbstractUserDetailsBuilder}
 * implementation.
 *
 * @author Giuseppe Milazzo
 * @since 1.1.0
 */
public interface FjwtUserDetailsBuilderFactory
        extends Function<String, FjwtAbstractUserDetailsBuilder> {
}
