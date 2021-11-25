package com.enginious.fjwt.core;

import java.util.function.Function;

/**
 * The factory that creates all the instances of the {@link FjwtAbstractUserDetailsBuilder}
 * implementation
 */
public interface FjwtUserDetailsBuilderFactory
    extends Function<String, FjwtAbstractUserDetailsBuilder> {}
