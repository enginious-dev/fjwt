package it.enginious.fjwt.config;

import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import it.enginious.fjwt.core.FjwtOAuth2JwtAuthenticationConverter;

/**
 * Configures Fjwt as an OAuth 2.0 Resource Server.
 *
 * @author Giuseppe Milazzo
 * @since 4.2.0
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
@ConditionalOnClass(JwtDecoder.class)
@ConditionalOnProperty(
    prefix = "fjwt",
    name = "security-mode",
    havingValue = "OAUTH2_RESOURCE_SERVER")
public class FjwtOAuth2ResourceServerConfig {

  private final FjwtProperties fjwtProperties;

  /**
   * Registers the default Fjwt JWT authentication converter.
   *
   * @return the JWT authentication converter
   */
  @Bean
  @ConditionalOnMissingBean
  public FjwtOAuth2JwtAuthenticationConverter fjwtOAuth2JwtAuthenticationConverter() {
    return new FjwtOAuth2JwtAuthenticationConverter(fjwtProperties);
  }

  /**
   * Registers the stateless OAuth 2.0 Resource Server filter chain.
   *
   * @param httpSecurity HTTP security builder
   * @param authenticationConverter JWT authentication converter
   * @return the Resource Server filter chain
   */
  @Bean
  @ConditionalOnMissingBean(SecurityFilterChain.class)
  public SecurityFilterChain fjwtOAuth2ResourceServerSecurityFilterChain(
      HttpSecurity httpSecurity, FjwtOAuth2JwtAuthenticationConverter authenticationConverter) {

    log.debug("configuring Fjwt as an OAuth 2.0 Resource Server");
    httpSecurity
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(fjwtProperties.getUnsecured().toArray(String[]::new))
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(authenticationConverter)))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .headers(
            headers ->
                headers.frameOptions(
                    frameOptions -> {
                      if (Objects.nonNull(fjwtProperties.getFrameOptions())
                          && fjwtProperties.getFrameOptions().isDisabled()) {
                        frameOptions.disable();
                      } else if (Objects.nonNull(fjwtProperties.getFrameOptions())
                          && fjwtProperties.getFrameOptions().isSameOrigin()) {
                        frameOptions.sameOrigin();
                      }
                    }));

    return httpSecurity.build();
  }
}
