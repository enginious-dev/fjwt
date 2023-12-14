package it.enginious.fjwt.core;

import it.enginious.fjwt.core.exceptions.FjwtTokenInvalidatorException;
import it.enginious.fjwt.dto.FjwtRequest;
import it.enginious.fjwt.dto.FjwtResponse;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FjwtControllerTest {

    @InjectMocks
    private FjwtController target;

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private FjwtTokenUtil fjwtTokenUtil;
    @Mock
    private FjwtTokenInvalidator fjwtTokenInvalidator;

    @Test
    void
    whenCreateAuthenticationTokenAndAuthenticationManagerDoesNotThrowThenShouldReturn200ResponseWithToken() {

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        User.builder()
                                .username("username")
                                .password("password")
                                .authorities(Collections.emptyList())
                                .build(),
                        null,
                        Collections.emptyList());
        User user =
                new User(
                        "username", (new BCryptPasswordEncoder()).encode("password"), Collections.emptyList());

        given(
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken("username", "password")))
                .willReturn(authentication);

        given(fjwtTokenUtil.generateToken(user)).willReturn("token");

        ResponseEntity<FjwtResponse> authenticationToken =
                target.createAuthenticationToken(
                        FjwtRequest.builder().username("username").password("password").build());

        assertThat(authenticationToken).isNotNull();
        assertThat(authenticationToken.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(authenticationToken.getBody()).isNotNull();
        assertThat(authenticationToken.getBody().getToken()).isEqualTo("token");
    }

    @Test
    void
    whenCreateAuthenticationTokenAndAuthenticationManagerDoesNotThrowThenShouldReturn401Response() {

        Authentication authentication = new UsernamePasswordAuthenticationToken("username", "password");

        given(authenticationManager.authenticate(authentication))
                .willThrow(new BadCredentialsException(StringUtils.EMPTY));

        ResponseEntity<FjwtResponse> authenticationToken =
                target.createAuthenticationToken(
                        FjwtRequest.builder().username("username").password("password").build());

        assertThat(authenticationToken).isNotNull();
        assertThat(authenticationToken.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(authenticationToken.getBody()).isNull();
    }

    @Test
    void whenCreateAuthenticationTokenAndFjwtTokenInvalidatorRaiseExceptionShouldReturn401Response()
            throws FjwtTokenInvalidatorException {

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        User.builder()
                                .username("username")
                                .password("password")
                                .authorities(Collections.emptyList())
                                .build(),
                        null,
                        Collections.emptyList());
        User user =
                new User(
                        "username", (new BCryptPasswordEncoder()).encode("password"), Collections.emptyList());

        given(
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken("username", "password")))
                .willReturn(authentication);

        doThrow(new FjwtTokenInvalidatorException())
                .when(fjwtTokenInvalidator)
                .store(any(), anyString());

        given(fjwtTokenUtil.generateToken(user)).willReturn("token");

        ResponseEntity<FjwtResponse> authenticationToken =
                target.createAuthenticationToken(
                        FjwtRequest.builder().username("username").password("password").build());

        assertThat(authenticationToken).isNotNull();
        assertThat(authenticationToken.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(authenticationToken.getBody()).isNull();
    }
}
