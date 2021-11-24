package com.enginious.fjwt.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

import com.enginious.fjwt.core.extractors.AuthoritiesExtractor;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
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
import org.springframework.security.core.userdetails.UserDetails;

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
            new ClaimsExtractorChain(Collections.singletonList(new AuthoritiesExtractor())));
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
  void whenValidateTokenAndTokenIsValidShouldNotThrow() {

    String token =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VybmFtZSIsImV4cCI6MTYzNTM0MzIwMCwiaWF0IjoxNjM1MzM5NjAwfQ.PpkOJKaNaNgjkxuxg8VdRllxKRDlFOsdRVosoDiv6NE";
    UserDetails user = new User("username", "password", Collections.emptyList());

    try (MockedStatic<Jwts> mocked = mockStatic(Jwts.class)) {

      mocked
          .when(Jwts::parser)
          .thenReturn(
              new DefaultJwtParser()
                  .setClock(() -> Date.from(Instant.ofEpochMilli(1635339600000L))));

      given(fjwtConfig.getSecret()).willReturn("secret");

      given(fjwtConfig.getAlgorithm()).willReturn(SignatureAlgorithm.HS256);

      assertThatNoException().isThrownBy(() -> target.validateToken(token, user));
    }
  }

  @Test
  void whenValidateTokenAndTokenHasDifferentUsernameShouldThrowJwtException() {

    String token =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkaWZmZXJlbnQiLCJleHAiOjE2MzUzNDMyMDAsImlhdCI6MTYzNTMzOTYwMH0.WyPEUvaOMHkd24ihFghA0tPvxEh8_gEsyqHs0oaCgCA";
    UserDetails user = new User("username", "password", Collections.emptyList());

    try (MockedStatic<Jwts> mocked = mockStatic(Jwts.class)) {

      mocked
          .when(Jwts::parser)
          .thenReturn(
              new DefaultJwtParser()
                  .setClock(() -> Date.from(Instant.ofEpochMilli(1635339600000L))));

      given(fjwtConfig.getSecret()).willReturn("secret");

      given(fjwtConfig.getAlgorithm()).willReturn(SignatureAlgorithm.HS256);

      assertThatThrownBy(() -> target.validateToken(token, user))
          .isExactlyInstanceOf(JwtException.class)
          .hasMessageMatching(
              "^username from JWT \\[[a-z0-9]+] is different than expected \\[username]\\.$");
    }
  }

  @Test
  void whenValidateTokenAndTokenIsExpiredShouldThrowExpiredJwtException() {

    String token =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VybmFtZSIsImV4cCI6MTYzMDAwMzIwMCwiaWF0IjoxNjMwMDA5NjAwfQ.miT7GxnGv7fm6l2GZVYqPapZInhLUMh8Q3hXl5tnabM";
    UserDetails user = new User("username", "password", Collections.emptyList());
    String pattern =
        "^JWT expired at \\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z\\. Current time: \\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z, a difference of \\d+ milliseconds\\.\\s+Allowed clock skew: \\d+ milliseconds\\.$";

    given(fjwtConfig.getSecret()).willReturn("secret");

    given(fjwtConfig.getAlgorithm()).willReturn(SignatureAlgorithm.HS256);

    assertThatThrownBy(() -> target.validateToken(token, user))
        .isExactlyInstanceOf(ExpiredJwtException.class)
        .hasMessageMatching(pattern);
  }

  @Test
  void whenValidateTokenAndTokenIsSignedWithDifferentKeyShouldThrowSignatureException() {

    String token =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VybmFtZSIsImV4cCI6MTYzMDAwMzIwMCwiaWF0IjoxNjMwMDA5NjAwfQ.aechoT_1XYVm1GNdqUj9C4y3b7bqzcUj0mo-xeQAIjA";
    UserDetails user = new User("username", "password", Collections.emptyList());
    String pattern =
        "^JWT signature does not match locally computed signature\\. JWT validity cannot be asserted and should not be trusted\\.$";

    given(fjwtConfig.getSecret()).willReturn("secret");

    given(fjwtConfig.getAlgorithm()).willReturn(SignatureAlgorithm.HS256);

    assertThatThrownBy(() -> target.validateToken(token, user))
        .isExactlyInstanceOf(SignatureException.class)
        .hasMessageMatching(pattern);
  }

  @Test
  void whenGenerateTokenShouldReturnToken() {

    String token =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VybmFtZSIsImV4cCI6MTYzNTM0MzIwMCwiaWF0IjoxNjM1MzM5NjAwLCJhdXRob3JpdGllcyI6WyJhdXRoMSIsImF1dGgyIl19.2MZ-wPeRfrzS84iKXx55V7Hfbu7ti5aBRl-3FF4FLEY";

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
