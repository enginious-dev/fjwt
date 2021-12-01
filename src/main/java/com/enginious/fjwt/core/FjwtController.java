package com.enginious.fjwt.core;

import com.enginious.fjwt.dto.FjwtRequest;
import com.enginious.fjwt.dto.FjwtResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Jwt authentication controller.
 *
 * @since 1.0.0
 * @author Giuseppe Milazzo
 */
@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
public class FjwtController {

  private final AuthenticationManager authenticationManager;
  private final FjwtTokenUtil fjwtTokenUtil;

  /**
   * Authentication endpoint, you can set this path through {@link FjwtConfig#endpoint}.
   *
   * @param request a {@link FjwtRequest}
   * @return the authentication response which is {@link HttpStatus#OK} in case of success and
   *     {@link HttpStatus#UNAUTHORIZED} in case of failure.
   */
  @PostMapping("${fjwt.endpoint:/authenticate}")
  public ResponseEntity<FjwtResponse> createAuthenticationToken(@RequestBody FjwtRequest request) {

    try {
      log.debug("processing request for user [{}]", request.getUsername());
      return ResponseEntity.ok(
          FjwtResponse.builder()
              .token(
                  fjwtTokenUtil.generateToken(
                      (UserDetails)
                          (authenticationManager.authenticate(
                                  new UsernamePasswordAuthenticationToken(
                                      request.getUsername(), request.getPassword())))
                              .getPrincipal()))
              .build());

    } catch (AuthenticationException e) {
      log.error(
          String.format(
              "error occurred while processing request for user [%s]:", request.getUsername()),
          e);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }
}
