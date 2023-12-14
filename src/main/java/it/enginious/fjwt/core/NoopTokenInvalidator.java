package it.enginious.fjwt.core;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * A noop token invalidator that does nothing
 *
 * @author Giuseppe Milazzo
 * @since 1.2.0
 */
public final class NoopTokenInvalidator implements FjwtTokenInvalidator {

    /**
     * {@inheritDoc}
     */
    @Override
    public void store(UserDetails source, String token) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean wasInvalidated(UserDetails source, String token) {
        return false;
    }
}
