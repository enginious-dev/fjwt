package com.enginious.fjwt.core;

import io.jsonwebtoken.JwtException;
import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Check if a token is supplied in request and its validity. If a valid token is found the
 * authentication is added in the {@link org.springframework.security.core.context.SecurityContext}.
 */
@Component
@RequiredArgsConstructor
public class FjwtRequestFilter extends OncePerRequestFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final Pattern TOKEN_PATTERN = Pattern.compile("^Bearer (.+)$");
  private static final int TOKEN_GROUP = 1;

  private final FjwtTokenUtil fjwtTokenUtil;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    final String requestTokenHeader = request.getHeader(AUTHORIZATION_HEADER);

    Matcher matcher =
        TOKEN_PATTERN.matcher(StringUtils.defaultIfBlank(requestTokenHeader, StringUtils.EMPTY));

    if (matcher.matches()) {
      UserDetails userDetails = null;
      String jwtToken = StringUtils.trim(matcher.group(TOKEN_GROUP));

      try {
        userDetails = fjwtTokenUtil.getUserFromToken(jwtToken);
      } catch (JwtException | IllegalArgumentException e) {
        logger.warn("exception occurred while parsing JWT token: ", e);
      }

      if (Objects.nonNull(userDetails)
          && SecurityContextHolder.getContext().getAuthentication() == null) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        usernamePasswordAuthenticationToken.setDetails(
            new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
      }
    } else {
      logger.warn("Invalid JWT token value");
    }
    chain.doFilter(request, response);
  }
}
