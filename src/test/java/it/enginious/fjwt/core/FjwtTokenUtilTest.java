package it.enginious.fjwt.core;

import ch.qos.logback.classic.Level;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.lang.Registry;
import io.jsonwebtoken.security.*;
import it.enginious.fjwt.core.extractors.FjwtAuthoritiesExtractor;
import nl.altindag.log.LogCaptor;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class FjwtTokenUtilTest {

    private FjwtTokenUtil target;

    @Mock
    private FjwtConfig fjwtConfig;

    @Mock
    private Clock clock;

    @Mock
    private SecretKey key;

    @Mock
    private Base64.Encoder encoder;

    @Mock
    @SuppressWarnings("rawtypes")
    private Registry<String, SecureDigestAlgorithm> mockAlgorithmRegistry;

    @Mock
    private MacAlgorithm mockMacAlgorithm;
    @Mock
    private SecretKeyBuilder mockSecretKeyBuilder;

    @BeforeEach
    void setup() {
        target = new FjwtTokenUtil(clock, fjwtConfig, new FjwtClaimsExtractorChain(Collections.singletonList(new FjwtAuthoritiesExtractor())), FjwtSimpleUserDetailsBuilder::new);
    }

    @ParameterizedTest
    @CsvSource({"TRACE,no secret provided: 8x/A?D(G+KbPeShVmYq3t6w9y$B&E)H@ (generated with algorithm HS256) will be used", "INFO,no secret provided: ******************************** (generated with algorithm HS256) will be used"})
    void whenEmptySecretIsSuppliedShouldLogPlaintextOrMaskedGeneratedSecretAccordingToLogLevel(String level, String expectedMessage) {

        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(FjwtTokenUtil.class)).setLevel(Level.valueOf(level));

        try (LogCaptor logCaptor = LogCaptor.forClass(FjwtTokenUtil.class); MockedStatic<Base64> mockedBase64 = mockStatic(Base64.class); MockedStatic<Jwts.SIG> mockedSig = mockStatic(Jwts.SIG.class)) {

            mockedSig.when(SIG::get).thenReturn(mockAlgorithmRegistry);
            given(mockAlgorithmRegistry.forKey(anyString())).willReturn(mockMacAlgorithm);
            given(mockMacAlgorithm.key()).willReturn(mockSecretKeyBuilder);
            given(mockSecretKeyBuilder.build()).willReturn(key);

            given(key.getEncoded()).willReturn(new byte[]{});

            mockedBase64.when(Base64::getEncoder).thenReturn(encoder);

            given(encoder.encode(any(byte[].class))).willReturn("8x/A?D(G+KbPeShVmYq3t6w9y$B&E)H@".getBytes(StandardCharsets.UTF_8));

            given(fjwtConfig.getSecret()).willReturn("");
            given(fjwtConfig.getAlgorithm()).willReturn("HS256");

            assertThatNoException().isThrownBy(() -> target.init());
            assertThat(logCaptor.getWarnLogs()).contains(expectedMessage);
        }
    }

    @ParameterizedTest
    @CsvSource({"TRACE,secret provided: 8x/A?D(G+KbPeShVmYq3t6w9y$B&E)H@ will be used", "INFO,secret provided: ******************************** will be used"})
    void whenNotEmptySecretIsSuppliedShouldLogPlaintextOrMaskedSuppliedSecretAccordingToLogLevel(String level, String expectedMessage) {

        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(FjwtTokenUtil.class)).setLevel(Level.valueOf(level));

        try (LogCaptor logCaptor = LogCaptor.forClass(FjwtTokenUtil.class); MockedStatic<Base64> mockedBase64 = mockStatic(Base64.class); MockedStatic<Jwts.SIG> mockedSig = mockStatic(Jwts.SIG.class)) {

            mockedSig.when(SIG::get).thenReturn(mockAlgorithmRegistry);
            given(mockAlgorithmRegistry.forKey(anyString())).willReturn(mockMacAlgorithm);
            given(mockMacAlgorithm.key()).willReturn(mockSecretKeyBuilder);
            given(mockSecretKeyBuilder.build()).willReturn(key);

            given(key.getEncoded()).willReturn(new byte[]{});

            mockedBase64.when(Base64::getEncoder).thenReturn(encoder);

            given(encoder.encode(any(byte[].class))).willReturn("MbQeThWmZq4t7w!z%C*F)J@NcRfUjXn2".getBytes(StandardCharsets.UTF_8));

            given(fjwtConfig.getSecret()).willReturn("8x/A?D(G+KbPeShVmYq3t6w9y$B&E)H@");
            given(fjwtConfig.getAlgorithm()).willReturn("HS256");

            assertThatNoException().isThrownBy(() -> target.init());
            assertThat(logCaptor.getInfoLogs()).contains(expectedMessage);
        }
    }

    @Test
    void whenWeakSecretIsSuppliedShouldThrowIllegalStateException() {
        try (MockedStatic<Base64> mockedBase64 = mockStatic(Base64.class)) {
            mockedBase64.when(Base64::getEncoder).thenReturn(encoder);
            given(encoder.encode(any(byte[].class))).willReturn("MbQeThWmZq4t7w!z%C*F)J@NcRfUjXn2".getBytes(StandardCharsets.UTF_8));
            given(fjwtConfig.getSecret()).willReturn("weak_secret");
            given(fjwtConfig.getAlgorithm()).willReturn("HS256");
            assertThatThrownBy(() -> target.init()).isExactlyInstanceOf(IllegalStateException.class).hasMessage("fjwt key validation failed").hasCauseExactlyInstanceOf(WeakKeyException.class);
        }
    }

    @Test
    void whenEmptyAlgorithmIsSuppliedShouldUseHS256() {

        try (LogCaptor logCaptor = LogCaptor.forClass(FjwtTokenUtil.class); MockedStatic<Base64> mockedBase64 = mockStatic(Base64.class); MockedStatic<Jwts.SIG> mockedSig = mockStatic(Jwts.SIG.class)) {

            mockedSig.when(SIG::get).thenReturn(mockAlgorithmRegistry);
            given(mockAlgorithmRegistry.forKey(anyString())).willReturn(mockMacAlgorithm);
            given(mockMacAlgorithm.key()).willReturn(mockSecretKeyBuilder);
            given(mockSecretKeyBuilder.build()).willReturn(key);

            given(key.getEncoded()).willReturn(new byte[]{});

            mockedBase64.when(Base64::getEncoder).thenReturn(encoder);

            given(encoder.encode(any(byte[].class))).willReturn("MbQeThWmZq4t7w!z%C*F)J@NcRfUjXn2".getBytes(StandardCharsets.UTF_8));

            given(fjwtConfig.getSecret()).willReturn("8x/A?D(G+KbPeShVmYq3t6w9y$B&E)H@");
            given(fjwtConfig.getAlgorithm()).willReturn(null);

            assertThatNoException().isThrownBy(() -> target.init());
            assertThat(logCaptor.getWarnLogs()).contains("no algorithm provided: HS256 will be used");
        }
    }

    @Test
    void whenAlgorithmIsSuppliedShouldUseTheChosenOne() {

        try (LogCaptor logCaptor = LogCaptor.forClass(FjwtTokenUtil.class); MockedStatic<Base64> mockedBase64 = mockStatic(Base64.class); MockedStatic<Jwts.SIG> mockedSig = mockStatic(Jwts.SIG.class)) {

            mockedSig.when(SIG::get).thenReturn(mockAlgorithmRegistry);
            given(mockAlgorithmRegistry.forKey(anyString())).willReturn(mockMacAlgorithm);
            given(mockMacAlgorithm.key()).willReturn(mockSecretKeyBuilder);
            given(mockSecretKeyBuilder.build()).willReturn(key);

            given(key.getEncoded()).willReturn(new byte[]{});

            mockedBase64.when(Base64::getEncoder).thenReturn(encoder);

            given(encoder.encode(any(byte[].class))).willReturn("MbQeThWmZq4t7w!z%C*F)J@NcRfUjXn2".getBytes(StandardCharsets.UTF_8));

            given(fjwtConfig.getSecret()).willReturn("dRgUkXp2s5v8y/A?D(G+KbPeShVmYq3t6w9z$C&E)H@McQfTjWnZr4u7x!A%D*G-");
            given(fjwtConfig.getAlgorithm()).willReturn("HS512");

            assertThatNoException().isThrownBy(() -> target.init());
            assertThat(logCaptor.getWarnLogs()).doesNotHave(new Condition<>(lines -> lines.stream().anyMatch(line -> StringUtils.startsWith(line, "no algorithm provided")), "no_algorithm_provided_condition"));
        }
    }

    @Test
    void whenGetUsernameFromTokenShouldReturnCorrectUsername() {

        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VybmFtZSIsImV4cCI6MTYzNTM0MzIwMCwiaWF0IjoxNjM1MzM5NjAwfQ.VGjklZbsJKzM2CQAanAXkLD81a4Az9OR2Cuhk2CoCcE";

        given(fjwtConfig.getSecret()).willReturn("8x/A?D(G+KbPeShVmYq3t6w9y$B&E)H@");

        given(fjwtConfig.getAlgorithm()).willReturn("HS256");

        given(clock.instant()).willReturn(Instant.ofEpochMilli(1635339600000L));

        target.init();

        assertThat(target.getUsernameFromToken(token)).isEqualTo("username");
    }

    @Test
    void whenExpirationDateFromTokenShouldReturnCorrectDate() {

        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VybmFtZSIsImV4cCI6MTYzNTM0MzIwMCwiaWF0IjoxNjM1MzM5NjAwfQ.VGjklZbsJKzM2CQAanAXkLD81a4Az9OR2Cuhk2CoCcE";

        given(fjwtConfig.getSecret()).willReturn("8x/A?D(G+KbPeShVmYq3t6w9y$B&E)H@");

        given(fjwtConfig.getAlgorithm()).willReturn("HS256");

        given(clock.instant()).willReturn(Instant.ofEpochMilli(1635339600000L));

        target.init();

        assertThat(target.getExpirationDateFromToken(token)).isEqualTo(Date.from(Instant.ofEpochMilli(1635343200000L)));
    }

    @Test
    void whenGenerateTokenShouldReturnToken() {

        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdXRob3JpdGllcyI6WyJhdXRoMSIsImF1dGgyIl0sInN1YiI6InVzZXJuYW1lIiwiaWF0IjoxNjM1MzM5NjAwLCJleHAiOjE2MzUzNDMyMDB9.inygt8sOsMTgJ1D0CMKv_le5yQw83GGvmMKfiXj2sl0";

        given(clock.instant()).willReturn(Instant.ofEpochMilli(1635339600000L));

        given(clock.getZone()).willReturn(ZoneId.systemDefault());

        given(fjwtConfig.getTtl()).willReturn(3600);

        given(fjwtConfig.getSecret()).willReturn("8x/A?D(G+KbPeShVmYq3t6w9y$B&E)H@");

        given(fjwtConfig.getAlgorithm()).willReturn("HS256");

        target.init();

        assertThat(target.generateToken(new User("username", "password", Arrays.asList(new SimpleGrantedAuthority("auth1"), new SimpleGrantedAuthority("auth2"))))).isEqualTo(token);
    }
}
