package com.enginious.fjwt.core;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * The default implementation for {@link FjwtAbstractUserDetailsBuilder} that returns an on the fly
 * built {@link UserDetails} with standard properties.
 *
 * @since 1.1.0
 * @author Giuseppe Milazzo
 */
public class FjwtSimpleUserDetailsBuilder extends FjwtAbstractUserDetailsBuilder {

  /**
   * Construct a builder with specified username
   *
   * @param username the username
   */
  public FjwtSimpleUserDetailsBuilder(String username) {
    super(username);
  }

  /** {@inheritDoc} */
  @Override
  public UserDetails build() {
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
