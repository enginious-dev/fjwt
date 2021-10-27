package com.enginious.fjwt.core;

import com.enginious.fjwt.dto.FjwtRequest;
import com.enginious.fjwt.dto.FjwtResponse;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FjwtControllerTest {

    @InjectMocks
    private FjwtController target;

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private FjwtTokenUtil fjwtTokenUtil;

    @Test
    void whenCreateAuthenticationTokenAndAuthenticationManagerDoesNotThrowThenShouldReturn200ResponseWithToken() {

        Authentication authentication = new UsernamePasswordAuthenticationToken("username", "password");
        User user = new User("username", (new BCryptPasswordEncoder()).encode("password"), Collections.emptyList());

        given(authenticationManager.authenticate(authentication))
                .willReturn(authentication);

        given(userDetailsService.loadUserByUsername("username"))
                .willReturn(user);

        given(fjwtTokenUtil.generateToken(user))
                .willReturn("token");

        ResponseEntity<FjwtResponse> authenticationToken = target.createAuthenticationToken(
                FjwtRequest
                        .builder()
                        .username("username")
                        .password("password")
                        .build()
        );

        assertThat(authenticationToken).isNotNull();
        assertThat(authenticationToken.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(authenticationToken.getBody()).isNotNull();
        assertThat(authenticationToken.getBody().getToken()).isEqualTo("token");
    }

    @Test
    void whenCreateAuthenticationTokenAndAuthenticationManagerDoesNotThrowThenShouldReturn401Response() {

        Authentication authentication = new UsernamePasswordAuthenticationToken("username", "password");

        given(authenticationManager.authenticate(authentication))
                .willThrow(new BadCredentialsException(StringUtils.EMPTY));

        ResponseEntity<FjwtResponse> authenticationToken = target.createAuthenticationToken(
                FjwtRequest
                        .builder()
                        .username("username")
                        .password("password")
                        .build()
        );

        assertThat(authenticationToken).isNotNull();
        assertThat(authenticationToken.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(authenticationToken.getBody()).isNull();
    }
}
