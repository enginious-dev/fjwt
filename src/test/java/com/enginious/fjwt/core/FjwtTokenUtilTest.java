package com.enginious.fjwt.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

import com.enginious.fjwt.core.extractors.FjwtAuthoritiesExtractor;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultJwtParser;
import io.jsonwebtoken.impl.crypto.MacProvider;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import javax.crypto.SecretKey;
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

  @Mock private SecretKey key;

  @Mock private Base64.Encoder encoder;

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
  void whenEmptySecretIsSuppliedShouldGenerateNewSecret() {

    String token =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VybmFtZSIsImV4cCI6MTYzNTM0MzIwMCwiaWF0IjoxNjM1MzM5NjAwfQ.TbTaDSlDqRylpk3h59u0qKTnkHXQ0U5uWDfF9lxgZFM";

    try (MockedStatic<Jwts> mockedJwts = mockStatic(Jwts.class);
        MockedStatic<Base64> mockedBase64 = mockStatic(Base64.class);
        MockedStatic<MacProvider> mockedMacProvider = mockStatic(MacProvider.class)) {

      mockedMacProvider
          .when(() -> MacProvider.generateKey(SignatureAlgorithm.HS256))
          .thenReturn(key);

      given(key.getEncoded()).willReturn(new byte[] {});

      mockedBase64.when(Base64::getEncoder).thenReturn(encoder);

      given(encoder.encode(any(byte[].class)))
          .willReturn("random-key".getBytes(StandardCharsets.UTF_8));

      given(fjwtConfig.getSecret()).willReturn("");

      given(fjwtConfig.getAlgorithm()).willReturn(SignatureAlgorithm.HS256);

      mockedJwts
          .when(Jwts::parser)
          .thenReturn(
              new DefaultJwtParser()
                  .setClock(() -> Date.from(Instant.ofEpochMilli(1635339600000L))));

      target.init();

      assertThat(target.getUsernameFromToken(token)).isEqualTo("username");
    }
  }

  @Test
  void whenGetUsernameFromTokenShouldReturnCorrectUsername() {

    String token =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VybmFtZSIsImV4cCI6MTYzNTM0MzIwMCwiaWF0IjoxNjM1MzM5NjAwfQ.PpkOJKaNaNgjkxuxg8VdRllxKRDlFOsdRVosoDiv6NE";

    try (MockedStatic<Jwts> mocked = mockStatic(Jwts.class)) {

      given(fjwtConfig.getSecret()).willReturn("secret");

      given(fjwtConfig.getAlgorithm()).willReturn(SignatureAlgorithm.HS256);

      target.init();

      mocked
          .when(Jwts::parser)
          .thenReturn(
              new DefaultJwtParser()
                  .setClock(() -> Date.from(Instant.ofEpochMilli(1635339600000L))));

      assertThat(target.getUsernameFromToken(token)).isEqualTo("username");
    }
  }

  @Test
  void whenExpirationDateFromTokenShouldReturnCorrectDate() {

    String token =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VybmFtZSIsImV4cCI6MTYzNTM0MzIwMCwiaWF0IjoxNjM1MzM5NjAwfQ.PpkOJKaNaNgjkxuxg8VdRllxKRDlFOsdRVosoDiv6NE";

    try (MockedStatic<Jwts> mocked = mockStatic(Jwts.class)) {

      given(fjwtConfig.getSecret()).willReturn("secret");

      given(fjwtConfig.getAlgorithm()).willReturn(SignatureAlgorithm.HS256);

      target.init();

      mocked
          .when(Jwts::parser)
          .thenReturn(
              new DefaultJwtParser()
                  .setClock(() -> Date.from(Instant.ofEpochMilli(1635339600000L))));

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

    given(fjwtConfig.getTtl()).willReturn(3600);

    given(fjwtConfig.getSecret()).willReturn("secret");

    given(fjwtConfig.getAlgorithm()).willReturn(SignatureAlgorithm.HS256);

    target.init();

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
