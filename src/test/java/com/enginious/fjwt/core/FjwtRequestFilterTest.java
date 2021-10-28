package com.enginious.fjwt.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.doNothing;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mockStatic;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.times;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.io.IOException;
import java.util.Collections;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class FjwtRequestFilterTest {

  @InjectMocks private FjwtRequestFilter target;

  @Mock private UserDetailsService userDetailsService;

  @Mock private FjwtTokenUtil fjwtTokenUtil;

  @Mock private FilterChain filterChain;

  @Mock private HttpServletRequest httpServletRequest;

  @Mock private HttpServletResponse httpServletResponse;

  @Captor private ArgumentCaptor<HttpServletRequest> httpServletRequestCaptor;

  @Captor private ArgumentCaptor<HttpServletResponse> httpServletResponseCaptor;

  @Test
  void
      whenDoFilterInternalAndRequestDoesNotContainsAuthorizationHeaderTokenShouldSkipAuthentication()
          throws ServletException, IOException {

    try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {

      given(httpServletRequest.getHeader("Authorization")).willReturn(null);

      target.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

      then(fjwtTokenUtil).should(never()).getUsernameFromToken(anyString());

      then(userDetailsService).should(never()).loadUserByUsername(anyString());

      then(fjwtTokenUtil).should(never()).validateToken(anyString(), any());

      mocked.verify(SecurityContextHolder::getContext, never());

      then(filterChain)
          .should(times(1))
          .doFilter(httpServletRequestCaptor.capture(), httpServletResponseCaptor.capture());

      assertThat(httpServletRequestCaptor.getValue()).isEqualTo(httpServletRequest);
      assertThat(httpServletResponseCaptor.getValue()).isEqualTo(httpServletResponse);
    }
  }

  @Test
  void
      whenDoFilterInternalAndRequestContainsAuthorizationHeaderWithInvalidTokenShouldSkipAuthentication()
          throws ServletException, IOException {

    try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {

      given(httpServletRequest.getHeader("Authorization")).willReturn(StringUtils.EMPTY);

      target.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

      then(fjwtTokenUtil).should(never()).getUsernameFromToken(anyString());

      then(userDetailsService).should(never()).loadUserByUsername(anyString());

      then(fjwtTokenUtil).should(never()).validateToken(anyString(), any());

      mocked.verify(SecurityContextHolder::getContext, never());

      then(filterChain)
          .should(times(1))
          .doFilter(httpServletRequestCaptor.capture(), httpServletResponseCaptor.capture());

      assertThat(httpServletRequestCaptor.getValue()).isEqualTo(httpServletRequest);
      assertThat(httpServletResponseCaptor.getValue()).isEqualTo(httpServletResponse);
    }
  }

  @Test
  void
      whenDoFilterInternalAndRequestContainsAuthorizationHeaderWithValidTokenAndFjwtTokenUtilThrowsIllegalArgumentExceptionShouldAbortAuthentication()
          throws ServletException, IOException {

    try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {

      given(httpServletRequest.getHeader("Authorization")).willReturn("Bearer token");

      given(fjwtTokenUtil.getUsernameFromToken("token")).willThrow(new IllegalArgumentException());

      target.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

      then(fjwtTokenUtil).should(times(1)).getUsernameFromToken("token");

      then(userDetailsService).should(never()).loadUserByUsername(anyString());

      then(fjwtTokenUtil).should(never()).validateToken(anyString(), any());

      mocked.verify(SecurityContextHolder::getContext, never());

      then(filterChain)
          .should(times(1))
          .doFilter(httpServletRequestCaptor.capture(), httpServletResponseCaptor.capture());

      assertThat(httpServletRequestCaptor.getValue()).isEqualTo(httpServletRequest);
      assertThat(httpServletResponseCaptor.getValue()).isEqualTo(httpServletResponse);
    }
  }

  @Test
  void
      whenDoFilterInternalAndRequestContainsAuthorizationHeaderWithValidTokenAndFjwtTokenUtilThrowsExpiredJwtExceptionShouldAbortAuthentication()
          throws ServletException, IOException {

    try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {

      given(httpServletRequest.getHeader("Authorization")).willReturn("Bearer token");

      given(fjwtTokenUtil.getUsernameFromToken("token"))
          .willThrow(new ExpiredJwtException(null, null, null));

      target.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

      then(fjwtTokenUtil).should(times(1)).getUsernameFromToken("token");

      then(userDetailsService).should(never()).loadUserByUsername(anyString());

      then(fjwtTokenUtil).should(never()).validateToken(anyString(), any());

      mocked.verify(SecurityContextHolder::getContext, never());

      then(filterChain)
          .should(times(1))
          .doFilter(httpServletRequestCaptor.capture(), httpServletResponseCaptor.capture());

      assertThat(httpServletRequestCaptor.getValue()).isEqualTo(httpServletRequest);
      assertThat(httpServletResponseCaptor.getValue()).isEqualTo(httpServletResponse);
    }
  }

  @Test
  void
      whenDoFilterInternalAndRequestContainsAuthorizationHeaderWithValidTokenAndFjwtTokenUtilReturnsAnEmptyUsernameShouldAbortAuthentication()
          throws ServletException, IOException {

    try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {

      given(httpServletRequest.getHeader("Authorization")).willReturn("Bearer token");

      given(fjwtTokenUtil.getUsernameFromToken("token")).willReturn(StringUtils.EMPTY);

      target.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

      then(fjwtTokenUtil).should(times(1)).getUsernameFromToken("token");

      then(userDetailsService).should(never()).loadUserByUsername(anyString());

      then(fjwtTokenUtil).should(never()).validateToken(anyString(), any());

      mocked.verify(SecurityContextHolder::getContext, never());

      then(filterChain)
          .should(times(1))
          .doFilter(httpServletRequestCaptor.capture(), httpServletResponseCaptor.capture());

      assertThat(httpServletRequestCaptor.getValue()).isEqualTo(httpServletRequest);
      assertThat(httpServletResponseCaptor.getValue()).isEqualTo(httpServletResponse);
    }
  }

  @Test
  void
      whenDoFilterInternalAndRequestContainsAuthorizationHeaderWithValidTokenAndSecurityContextHolderReturnsContextWithAuthenticationShouldAbortAuthentication()
          throws ServletException, IOException {

    try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {

      SecurityContext securityContext =
          new SecurityContextImpl(new UsernamePasswordAuthenticationToken(null, null, null));

      given(httpServletRequest.getHeader("Authorization")).willReturn("Bearer token");

      given(fjwtTokenUtil.getUsernameFromToken("token")).willReturn("username");

      mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      target.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

      then(fjwtTokenUtil).should(times(1)).getUsernameFromToken("token");

      then(userDetailsService).should(never()).loadUserByUsername(anyString());

      then(fjwtTokenUtil).should(never()).validateToken(anyString(), any());

      mocked.verify(SecurityContextHolder::getContext, times(1));

      then(filterChain)
          .should(times(1))
          .doFilter(httpServletRequestCaptor.capture(), httpServletResponseCaptor.capture());

      assertThat(httpServletRequestCaptor.getValue()).isEqualTo(httpServletRequest);
      assertThat(httpServletResponseCaptor.getValue()).isEqualTo(httpServletResponse);
    }
  }

  @Test
  void
      whenDoFilterInternalAndRequestContainsAuthorizationHeaderWithValidTokenAndUserDetailsServiceThrowsExceptionShouldThrow()
          throws ServletException, IOException {

    try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {

      given(httpServletRequest.getHeader("Authorization")).willReturn("Bearer token");

      given(fjwtTokenUtil.getUsernameFromToken("token")).willReturn("username");

      mocked.when(SecurityContextHolder::getContext).thenReturn(new SecurityContextImpl(null));

      given(userDetailsService.loadUserByUsername("username"))
          .willThrow(new UsernameNotFoundException(null));

      assertThatThrownBy(
              () -> target.doFilterInternal(httpServletRequest, httpServletResponse, filterChain))
          .isExactlyInstanceOf(UsernameNotFoundException.class);

      then(fjwtTokenUtil).should(times(1)).getUsernameFromToken("token");

      then(userDetailsService).should(times(1)).loadUserByUsername(anyString());

      then(fjwtTokenUtil).should(never()).validateToken(anyString(), any());

      mocked.verify(SecurityContextHolder::getContext, times(1));

      then(filterChain).should(never()).doFilter(any(), any());
    }
  }

  @Test
  void
      whenDoFilterInternalAndRequestContainsAuthorizationHeaderWithValidTokenAndFjwtTokenUtilReturnFalseOnValidateTokenShouldAbortAuthentication()
          throws ServletException, IOException {

    try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {

      User user =
          new User(
              "username",
              (new BCryptPasswordEncoder()).encode("password"),
              Collections.emptyList());

      given(httpServletRequest.getHeader("Authorization")).willReturn("Bearer token");

      given(fjwtTokenUtil.getUsernameFromToken("token")).willReturn("username");

      mocked.when(SecurityContextHolder::getContext).thenReturn(new SecurityContextImpl(null));

      given(userDetailsService.loadUserByUsername("username")).willReturn(user);

      doThrow(new JwtException("")).when(fjwtTokenUtil).validateToken("token", user);

      assertThatThrownBy(
              () -> target.doFilterInternal(httpServletRequest, httpServletResponse, filterChain))
          .isExactlyInstanceOf(JwtException.class);

      then(fjwtTokenUtil).should(times(1)).getUsernameFromToken("token");

      then(userDetailsService).should(times(1)).loadUserByUsername(anyString());

      then(fjwtTokenUtil).should(times(1)).validateToken(anyString(), any());

      mocked.verify(SecurityContextHolder::getContext, times(1));

      then(filterChain).should(never()).doFilter(any(), any());
    }
  }

  @Test
  void
      whenDoFilterInternalAndRequestContainsAuthorizationHeaderWithValidTokenAndFjwtTokenUtilReturnTrueOnValidateTokenShouldAuthenticate()
          throws ServletException, IOException {

    try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {

      SecurityContextImpl securityContext = new SecurityContextImpl(null);
      User user =
          new User(
              "username",
              "$2a$10$mHxPfPszH48Q/31BIK8LIeBAm.s6FWTlhtWHb9.Dy56ujc6mfNIbS",
              Collections.emptyList());

      given(httpServletRequest.getHeader("Authorization")).willReturn("Bearer token");

      given(fjwtTokenUtil.getUsernameFromToken("token")).willReturn("username");

      mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      given(userDetailsService.loadUserByUsername("username")).willReturn(user);

      doNothing().when(fjwtTokenUtil).validateToken("token", user);

      target.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

      then(fjwtTokenUtil).should(times(1)).getUsernameFromToken("token");

      then(userDetailsService).should(times(1)).loadUserByUsername(anyString());

      then(fjwtTokenUtil).should(times(1)).validateToken(anyString(), any());

      mocked.verify(SecurityContextHolder::getContext, times(2));

      assertThat(securityContext.getAuthentication()).isNotNull();
      assertThat(((UserDetails) securityContext.getAuthentication().getPrincipal()).getUsername())
          .isEqualTo("username");
      assertThat(((UserDetails) securityContext.getAuthentication().getPrincipal()).getPassword())
          .isEqualTo("$2a$10$mHxPfPszH48Q/31BIK8LIeBAm.s6FWTlhtWHb9.Dy56ujc6mfNIbS");

      then(filterChain)
          .should(times(1))
          .doFilter(httpServletRequestCaptor.capture(), httpServletResponseCaptor.capture());

      assertThat(httpServletRequestCaptor.getValue()).isEqualTo(httpServletRequest);
      assertThat(httpServletResponseCaptor.getValue()).isEqualTo(httpServletResponse);
    }
  }
}
