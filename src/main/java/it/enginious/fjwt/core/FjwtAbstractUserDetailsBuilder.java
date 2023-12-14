package it.enginious.fjwt.core;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Base class for all Fjwt user details builder. In this scope a user details builder it's an object
 * used to build users starting from the claims retrieved from the token. In most cases the default
 * builder {@link FjwtSimpleUserDetailsBuilder} will be enough, but if you need to extend the
 * functionality you can easily implement your own version extending this class and declare a bean
 * of type {@link FjwtUserDetailsBuilderFactory} that supplies your specific implementation.
 *
 * @author Giuseppe Milazzo
 * @since 1.1.0
 */
@RequiredArgsConstructor
public abstract class FjwtAbstractUserDetailsBuilder {

    /**
     * The username
     */
    protected final String username;
    /**
     * The authorities
     */
    protected Collection<? extends GrantedAuthority> authorities = new ArrayList<>();
    /**
     * A flag for account expiration
     */
    protected boolean accountExpired = false;
    /**
     * A flag for account lock
     */
    protected boolean accountLocked = false;
    /**
     * A flag for credentials expired
     */
    protected boolean credentialsExpired = false;
    /**
     * A flag for account enabling
     */
    protected boolean enabled = true;

    /**
     * Builds the {@link UserDetails} based on a specific implementation.
     *
     * @return the user details object
     */
    public abstract UserDetails build();

    /**
     * Sets the {@link FjwtAbstractUserDetailsBuilder#authorities}
     *
     * @param authorities the authorities
     */
    public void authorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    /**
     * Sets the {@link FjwtAbstractUserDetailsBuilder#accountExpired} flag
     *
     * @param accountExpired the accountExpired flag
     */
    public void accountExpired(boolean accountExpired) {
        this.accountExpired = accountExpired;
    }

    /**
     * Sets the {@link FjwtAbstractUserDetailsBuilder#accountLocked} flag
     *
     * @param accountLocked the accountLocked flag
     */
    public void accountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    /**
     * Sets the {@link FjwtAbstractUserDetailsBuilder#credentialsExpired} flag
     *
     * @param credentialsExpired the credentialsExpired flag
     */
    public void credentialsExpired(boolean credentialsExpired) {
        this.credentialsExpired = credentialsExpired;
    }

    /**
     * Sets the {@link FjwtAbstractUserDetailsBuilder#enabled} flag
     *
     * @param enabled the enabled flag
     */
    public void enabled(boolean enabled) {
        this.enabled = enabled;
    }
}
