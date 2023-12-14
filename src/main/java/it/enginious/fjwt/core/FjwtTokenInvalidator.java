package it.enginious.fjwt.core;

import it.enginious.fjwt.core.exceptions.FjwtTokenInvalidatorException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * The logic for token invalidation
 *
 * @author Giuseppe Milazzo
 * @since 1.2.0
 */
public interface FjwtTokenInvalidator {

    /**
     * Stores token in external system
     *
     * @param source the user
     * @param token  the token
     * @throws FjwtTokenInvalidatorException if any error occurs
     */
    void store(UserDetails source, String token) throws FjwtTokenInvalidatorException;

    /**
     * Checks if the token was invalidated
     *
     * @param source the user
     * @param token  the token
     * @return true if the token was invalidated otherwise false
     * @throws FjwtTokenInvalidatorException if any error occurs
     */
    boolean wasInvalidated(UserDetails source, String token) throws FjwtTokenInvalidatorException;
}
