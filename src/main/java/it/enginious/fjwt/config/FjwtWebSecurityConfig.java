package it.enginious.fjwt.config;

import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import it.enginious.fjwt.core.FjwtEntryPoint;
import it.enginious.fjwt.core.FjwtRequestFilter;

/**
 * Fjwt web security configuration.
 *
 * @author Giuseppe Milazzo
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class FjwtWebSecurityConfig {

  private final FjwtEntryPoint fjwtEntryPoint;
  private final FjwtRequestFilter fjwtRequestFilter;
  private final FjwtProperties fjwtProperties;

  /**
   * register the {@link SecurityFilterChain}
   *
   * @param httpSecurity the http security
   * @return the security filter chain bean
   * @throws Exception if any error occurs
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
    log.debug(
        "configuring [{}]: paths that don't need authentication are [{}]",
        HttpSecurity.class.getName(),
        String.join(", ", fjwtProperties.getAllUnsecuredEndpoints()));

    httpSecurity
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            auth -> auth.requestMatchers(fjwtProperties.getAllUnsecuredEndpoints()).permitAll())
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        request -> request.getMethod().equalsIgnoreCase(HttpMethod.OPTIONS.name()))
                    .permitAll())
        .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
        .exceptionHandling(
            exceptionHandlingConfigurer ->
                exceptionHandlingConfigurer.authenticationEntryPoint(fjwtEntryPoint))
        .sessionManagement(
            sessionManagementConfigurer ->
                sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    httpSecurity.headers(
        httpSecurityHeadersConfigurer ->
            httpSecurityHeadersConfigurer.frameOptions(
                frameOptionsConfig -> {
                  if (Objects.nonNull(fjwtProperties.getFrameOptions())
                      && fjwtProperties.getFrameOptions().isDisabled()) {
                    log.debug("disabling frame since fjwt.frame-options.disabled is set to true");
                    frameOptionsConfig.disable();
                  } else {
                    log.debug("frame unchanged since fjwt.frame-options.disabled is set to false");
                    if (Objects.nonNull(fjwtProperties.getFrameOptions())
                        && fjwtProperties.getFrameOptions().isSameOrigin()) {
                      log.debug(
                          "enabling same-origin since fjwt.frame-options.same-origin is set to true");
                      frameOptionsConfig.sameOrigin();
                    } else {
                      log.debug(
                          "same-origin unchanged since fjwt.frame-options.same-origin is set to false");
                    }
                  }
                }));

    return httpSecurity
        .addFilterBefore(fjwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }
}
