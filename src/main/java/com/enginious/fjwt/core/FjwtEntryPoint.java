package com.enginious.fjwt.core;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * See {@link AuthenticationEntryPoint}.
 *
 * @since 1.0.0
 * @author Giuseppe Milazzo
 */
@Component
public class FjwtEntryPoint implements AuthenticationEntryPoint {

  /** {@inheritDoc} */
  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
  }
}
