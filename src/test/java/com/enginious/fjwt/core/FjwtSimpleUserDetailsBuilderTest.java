package com.enginious.fjwt.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

class FjwtSimpleUserDetailsBuilderTest {

  @Test
  void testAccessor() {

    FjwtSimpleUserDetailsBuilder builder = new FjwtSimpleUserDetailsBuilder("username");
    builder.authorities(Collections.singletonList(new SimpleGrantedAuthority("auth1")));
    builder.accountExpired(false);
    builder.accountLocked(false);
    builder.credentialsExpired(false);
    builder.enabled(true);
    UserDetails userDetails = builder.build();

    assertThat(userDetails.getUsername()).isEqualTo("username");
    assertThat(userDetails.getPassword()).isNull();
    assertThat(userDetails.isAccountNonExpired()).isTrue();
    assertThat(userDetails.isAccountNonLocked()).isTrue();
    assertThat(userDetails.isCredentialsNonExpired()).isTrue();
    assertThat(userDetails.isEnabled()).isTrue();
    assertThat(userDetails.getAuthorities()).hasSize(1);
    assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("auth1");
  }
}
