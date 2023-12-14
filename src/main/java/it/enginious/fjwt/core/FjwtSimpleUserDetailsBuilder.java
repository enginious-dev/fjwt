package it.enginious.fjwt.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * The default implementation for {@link FjwtAbstractUserDetailsBuilder} that returns an on the fly
 * built {@link UserDetails} with standard properties.
 *
 * @author Giuseppe Milazzo
 * @since 1.1.0
 */
@Slf4j
public class FjwtSimpleUserDetailsBuilder extends FjwtAbstractUserDetailsBuilder {

    /**
     * Construct a builder with specified username
     *
     * @param username the username
     */
    public FjwtSimpleUserDetailsBuilder(String username) {
        super(username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserDetails build() {
        log.debug("building user");
        return new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return authorities;
            }

            @Override
            public String getPassword() {
                return null;
            }

            @Override
            public String getUsername() {
                return username;
            }

            @Override
            public boolean isAccountNonExpired() {
                return !accountExpired;
            }

            @Override
            public boolean isAccountNonLocked() {
                return !accountLocked;
            }

            @Override
            public boolean isCredentialsNonExpired() {
                return !credentialsExpired;
            }

            @Override
            public boolean isEnabled() {
                return enabled;
            }
        };
    }
}
