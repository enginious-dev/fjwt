package it.enginious.fjwt.core;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * See {@link AuthenticationEntryPoint}.
 *
 * @author Giuseppe Milazzo
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnProperty(
    prefix = "fjwt",
    name = "security-mode",
    havingValue = "LEGACY_JWT",
    matchIfMissing = true)
public class FjwtEntryPoint implements AuthenticationEntryPoint {

  /** {@inheritDoc} */
  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    HttpStatus status =
        response.getStatus() == HttpStatus.FORBIDDEN.value()
            ? HttpStatus.FORBIDDEN
            : HttpStatus.UNAUTHORIZED;
    log.debug(
        "sending response with code [{}] and message [{}] for [{}] [{}]",
        status.value(),
        status.getReasonPhrase(),
        request.getMethod(),
        request.getRequestURI());
    response.sendError(status.value(), status.getReasonPhrase());
  }
}
