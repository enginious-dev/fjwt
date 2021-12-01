package com.enginious.fjwt.core;

import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * A dummy implementation for {@link UserDetailsService} that returns, for any username passed, a
 * user with username and password equal to the username passed
 *
 * @since 1.1.0
 * @author Giuseppe Milazzo
 */
@Slf4j
@RequiredArgsConstructor
public class FjwtDummyUserDetailsService implements UserDetailsService {

  private final PasswordEncoder passwordEncoder;

  /** {@inheritDoc} */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return new User(username, passwordEncoder.encode(username), Collections.emptyList());
  }
}
