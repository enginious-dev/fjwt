package com.enginious.fjwt.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

import com.enginious.fjwt.core.extractors.FjwtAuthoritiesExtractor;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultJwtParser;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

@ExtendWith(MockitoExtension.class)
class FjwtTokenUtilTest {

  private FjwtTokenUtil target;

  @Mock private FjwtConfig fjwtConfig;

  @Mock private Clock clock;

  @BeforeEach
  void setup() {
    target =
        new FjwtTokenUtil(
            clock,
            fjwtConfig,
            new FjwtClaimsExtractorChain(Collections.singletonList(new FjwtAuthoritiesExtractor())),
            FjwtSimpleUserDetailsBuilder::new);
  }

  @Test
  void whenGetUsernameFromTokenShouldReturnCorrectUsername() {

    String token =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VybmFtZSIsImV4cCI6MTYzNTM0MzIwMCwiaWF0IjoxNjM1MzM5NjAwfQ.PpkOJKaNaNgjkxuxg8VdRllxKRDlFOsdRVosoDiv6NE";

    try (MockedStatic<Jwts> mocked = mockStatic(Jwts.class)) {

      mocked
          .when(Jwts::parser)
          .thenReturn(
              new DefaultJwtParser()
                  .setClock(() -> Date.from(Instant.ofEpochMilli(1635339600000L))));

      given(fjwtConfig.getSecret()).willReturn("secret");

      given(fjwtConfig.getAlgorithm()).willReturn(SignatureAlgorithm.HS256);

      assertThat(target.getUsernameFromToken(token)).isEqualTo("username");
    }
  }

  @Test
  void whenExpirationDateFromTokenShouldReturnCorrectDate() {

    String token =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VybmFtZSIsImV4cCI6MTYzNTM0MzIwMCwiaWF0IjoxNjM1MzM5NjAwfQ.PpkOJKaNaNgjkxuxg8VdRllxKRDlFOsdRVosoDiv6NE";

    try (MockedStatic<Jwts> mocked = mockStatic(Jwts.class)) {

      mocked
          .when(Jwts::parser)
          .thenReturn(
              new DefaultJwtParser()
                  .setClock(() -> Date.from(Instant.ofEpochMilli(1635339600000L))));

      given(fjwtConfig.getSecret()).willReturn("secret");

      given(fjwtConfig.getAlgorithm()).willReturn(SignatureAlgorithm.HS256);

      assertThat(target.getExpirationDateFromToken(token))
          .isEqualTo(Date.from(Instant.ofEpochMilli(1635343200000L)));
    }
  }

  @Test
  void whenGenerateTokenShouldReturnToken() {

    String token =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdXRob3JpdGllcyI6WyJhdXRoMSIsImF1dGgyIl0sInN1YiI6InVzZXJuYW1lIiwiaWF0IjoxNjM1MzM5NjAwLCJleHAiOjE2MzUzNDMyMDB9.DrzwVfy-4uDnuXApRv_Rxt46xxWgsU2ab7DF7fO9QmM";

    given(clock.instant()).willReturn(Instant.ofEpochMilli(1635339600000L));

    given(clock.getZone()).willReturn(ZoneId.systemDefault());

    given(fjwtConfig.getSecret()).willReturn("secret");

    given(fjwtConfig.getAlgorithm()).willReturn(SignatureAlgorithm.HS256);

    given(fjwtConfig.getTtl()).willReturn(3600);

    assertThat(
            target.generateToken(
                new User(
                    "username",
                    "password",
                    Arrays.asList(
                        new SimpleGrantedAuthority("auth1"), new SimpleGrantedAuthority("auth2")))))
        .isEqualTo(token);
  }
}
