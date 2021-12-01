package com.enginious.fjwt.core;

import java.util.function.Function;

/**
 * The factory that creates all the instances of the {@link FjwtAbstractUserDetailsBuilder}
 * implementation.
 *
 * @since 1.1.0
 * @author Giuseppe Milazzo
 */
public interface FjwtUserDetailsBuilderFactory
    extends Function<String, FjwtAbstractUserDetailsBuilder> {}
