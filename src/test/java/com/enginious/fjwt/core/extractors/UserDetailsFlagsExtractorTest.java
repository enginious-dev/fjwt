package com.enginious.fjwt.core.extractors;

import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

class UserDetailsFlagsExtractorTest {

  private UserDetailsFlagsExtractor target = new UserDetailsFlagsExtractor();

  @Test
  void whenGetClaimsMapShouldContainsAllFlag() {
    Map<String, Object> claims = new HashMap<>();
    target.getClaims(
        User.builder()
            .username("test")
            .password("test")
            .authorities(new GrantedAuthority[] {})
            .build(),
        claims);
    Assertions.assertThat(claims).hasSizeGreaterThanOrEqualTo(4);
    Assertions.assertThat((Boolean) claims.get(UserDetailsFlagsExtractor.CREDENTIALS_EXPIRED))
        .isFalse();
    Assertions.assertThat((Boolean) claims.get(UserDetailsFlagsExtractor.ACCOUNT_EXPIRED))
        .isFalse();
    Assertions.assertThat((Boolean) claims.get(UserDetailsFlagsExtractor.ACCOUNT_LOCKED)).isFalse();
    Assertions.assertThat((Boolean) claims.get(UserDetailsFlagsExtractor.ENABLED)).isTrue();
  }
}
