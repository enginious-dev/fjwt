package it.enginious.fjwt.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.util.Collections;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import it.enginious.fjwt.core.exceptions.FjwtTokenInvalidatorException;

@ExtendWith(MockitoExtension.class)
class FjwtRequestFilterTest {

  @InjectMocks private FjwtRequestFilter target;

  @Mock private FjwtTokenUtil fjwtTokenUtil;

  @Mock private FjwtTokenInvalidator fjwtTokenInvalidator;

  @Mock private FjwtConfig fjwtConfig;

  @Mock private FilterChain filterChain;

  @Mock private HttpServletRequest httpServletRequest;

  @Mock private HttpServletResponse httpServletResponse;

  @Captor private ArgumentCaptor<HttpServletRequest> httpServletRequestCaptor;

  @Captor private ArgumentCaptor<HttpServletResponse> httpServletResponseCaptor;

  @BeforeEach
  void setUp() {
    doReturn(new String[] {"/authenticate"}).when(fjwtConfig).getAllUnsecuredEndpoints();
    target.init();
  }

  @Test
  void whenDoFilterInternalAndRequestHasOptionsMethodChainShouldBeBypassed()
      throws IOException, ServletException {
    try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {

      given(httpServletRequest.getMethod()).willReturn(HttpMethod.OPTIONS.name());

      target.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

      then(httpServletRequest).should(never()).getHeader(anyString());

      then(fjwtTokenUtil).should(never()).getUsernameFromToken(anyString());

      mocked.verify(SecurityContextHolder::getContext, never());

      then(filterChain)
          .should(times(1))
          .doFilter(httpServletRequestCaptor.capture(), httpServletResponseCaptor.capture());

      assertThat(httpServletRequestCaptor.getValue()).isEqualTo(httpServletRequest);
      assertThat(httpServletResponseCaptor.getValue()).isEqualTo(httpServletResponse);
    }
  }

  @Test
  void whenDoFilterInternalAndRequestDoesNotContainsAuthorizationHeaderShouldSkipAuthentication()
      throws ServletException, IOException {

    try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {

      given(fjwtTokenUtil.getTokenFromHeader(httpServletRequest)).willReturn(null);
      given(httpServletRequest.getMethod()).willReturn(HttpMethod.GET.name());

      target.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

      then(fjwtTokenUtil).should(never()).getUsernameFromToken(anyString());

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

      given(fjwtTokenUtil.getTokenFromHeader(httpServletRequest)).willReturn("token");
      given(httpServletRequest.getMethod()).willReturn(HttpMethod.GET.name());

      given(fjwtTokenUtil.getUserFromToken("token")).willThrow(new IllegalArgumentException());

      target.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

      then(fjwtTokenUtil).should(times(1)).getUserFromToken("token");

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

      given(fjwtTokenUtil.getTokenFromHeader(httpServletRequest)).willReturn("token");
      given(httpServletRequest.getMethod()).willReturn(HttpMethod.GET.name());

      given(fjwtTokenUtil.getUserFromToken("token"))
          .willThrow(new ExpiredJwtException(null, null, null));

      target.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

      then(fjwtTokenUtil).should(times(1)).getUserFromToken("token");

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
      whenDoFilterInternalAndRequestContainsAuthorizationHeaderWithValidTokenAndTokenWasInvalidatedTokenShouldAbortAuthentication()
          throws ServletException, IOException, FjwtTokenInvalidatorException {

    try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {

      SecurityContextImpl securityContext = new SecurityContextImpl(null);
      User user =
          new User(
              "username",
              "$2a$10$mHxPfPszH48Q/31BIK8LIeBAm.s6FWTlhtWHb9.Dy56ujc6mfNIbS",
              Collections.emptyList());

      given(fjwtTokenUtil.getTokenFromHeader(httpServletRequest)).willReturn("token");
      given(httpServletRequest.getMethod()).willReturn(HttpMethod.GET.name());

      given(fjwtTokenUtil.getUserFromToken("token")).willReturn(user);

      given(fjwtTokenInvalidator.wasInvalidated(user, "token")).willReturn(true);

      mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      target.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

      then(fjwtTokenUtil).should(times(1)).getUserFromToken("token");

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
      whenDoFilterInternalAndRequestContainsAuthorizationHeaderWithValidTokenAndTokenInvalidatorIsNoopTokenInvalidatorShouldBypassInvalidationCheck()
          throws ServletException, IOException {

    try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {

      DirectFieldAccessor dfa = new DirectFieldAccessor(target);

      NoopTokenInvalidator noopTokenInvalidator = Mockito.mock(NoopTokenInvalidator.class);

      dfa.setPropertyValue("fjwtTokenInvalidator", noopTokenInvalidator);

      SecurityContextImpl securityContext = new SecurityContextImpl(null);
      User user =
          new User(
              "username",
              "$2a$10$mHxPfPszH48Q/31BIK8LIeBAm.s6FWTlhtWHb9.Dy56ujc6mfNIbS",
              Collections.emptyList());

      given(fjwtTokenUtil.getTokenFromHeader(httpServletRequest)).willReturn("token");
      given(httpServletRequest.getMethod()).willReturn(HttpMethod.GET.name());

      given(fjwtTokenUtil.getUserFromToken("token")).willReturn(user);

      mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      target.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

      then(fjwtTokenUtil).should(times(1)).getUserFromToken("token");

      mocked.verify(SecurityContextHolder::getContext, times(2));

      assertThat(securityContext.getAuthentication()).isNotNull();
      assertThat(((UserDetails) securityContext.getAuthentication().getPrincipal()).getUsername())
          .isEqualTo("username");
      assertThat(((UserDetails) securityContext.getAuthentication().getPrincipal()).getPassword())
          .isEqualTo("$2a$10$mHxPfPszH48Q/31BIK8LIeBAm.s6FWTlhtWHb9.Dy56ujc6mfNIbS");

      then(noopTokenInvalidator).should(never()).wasInvalidated(any(), anyString());

      then(filterChain)
          .should(times(1))
          .doFilter(httpServletRequestCaptor.capture(), httpServletResponseCaptor.capture());

      assertThat(httpServletRequestCaptor.getValue()).isEqualTo(httpServletRequest);
      assertThat(httpServletResponseCaptor.getValue()).isEqualTo(httpServletResponse);

      dfa.setPropertyValue("fjwtTokenInvalidator", fjwtTokenInvalidator);
    }
  }

  @Test
  void
      whenDoFilterInternalAndRequestContainsAuthorizationHeaderWithValidTokenAndFjwtTokenUtilReturnTrueOnValidateTokenShouldAuthenticate()
          throws ServletException, IOException, FjwtTokenInvalidatorException {

    try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {

      SecurityContextImpl securityContext = new SecurityContextImpl(null);
      User user =
          new User(
              "username",
              "$2a$10$mHxPfPszH48Q/31BIK8LIeBAm.s6FWTlhtWHb9.Dy56ujc6mfNIbS",
              Collections.emptyList());

      given(fjwtTokenUtil.getTokenFromHeader(httpServletRequest)).willReturn("token");
      given(httpServletRequest.getMethod()).willReturn(HttpMethod.GET.name());

      given(fjwtTokenUtil.getUserFromToken("token")).willReturn(user);

      given(fjwtTokenInvalidator.wasInvalidated(user, "token")).willReturn(false);

      mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      target.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

      then(fjwtTokenUtil).should(times(1)).getUserFromToken("token");

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

  @Test
  void
      whenDoFilterInternalAndRequestContainsAuthorizationHeaderAndTokenInvalidatorRaiseExceptionShouldAbortAuthentication()
          throws ServletException, IOException, FjwtTokenInvalidatorException {

    try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {

      SecurityContextImpl securityContext = new SecurityContextImpl(null);
      User user =
          new User(
              "username",
              "$2a$10$mHxPfPszH48Q/31BIK8LIeBAm.s6FWTlhtWHb9.Dy56ujc6mfNIbS",
              Collections.emptyList());

      given(fjwtTokenUtil.getTokenFromHeader(httpServletRequest)).willReturn("token");
      given(httpServletRequest.getMethod()).willReturn(HttpMethod.GET.name());

      given(fjwtTokenUtil.getUserFromToken("token")).willReturn(user);

      given(fjwtTokenInvalidator.wasInvalidated(user, "token"))
          .willThrow(new FjwtTokenInvalidatorException());

      mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      target.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

      then(fjwtTokenUtil).should(times(1)).getUserFromToken("token");

      mocked.verify(SecurityContextHolder::getContext, times(1));

      then(filterChain)
          .should(times(1))
          .doFilter(httpServletRequestCaptor.capture(), httpServletResponseCaptor.capture());

      assertThat(httpServletRequestCaptor.getValue()).isEqualTo(httpServletRequest);
      assertThat(httpServletResponseCaptor.getValue()).isEqualTo(httpServletResponse);
    }
  }
}
