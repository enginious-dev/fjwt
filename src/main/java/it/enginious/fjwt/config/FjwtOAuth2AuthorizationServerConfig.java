package it.enginious.fjwt.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import it.enginious.fjwt.core.FjwtClaimsExtractorChain;
import it.enginious.fjwt.core.FjwtOAuth2JwtAuthenticationConverter;

/**
 * Configures Fjwt as an OAuth 2.0 Authorization Server with optional OpenID Connect support.
 *
 * @author Giuseppe Milazzo
 * @since 4.2.0
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
@ConditionalOnClass(OAuth2Authorization.class)
@ConditionalOnProperty(
    prefix = "fjwt",
    name = "security-mode",
    havingValue = "OAUTH2_AUTHORIZATION_SERVER")
public class FjwtOAuth2AuthorizationServerConfig {

  private static final Set<String> RESERVED_ACCESS_TOKEN_CLAIMS =
      Set.of("iss", "sub", "aud", "exp", "nbf", "iat", "jti", "client_id", "scope");

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
   * Adds Fjwt user claims to OAuth 2.0 access tokens without replacing reserved claims.
   *
   * @param claimsExtractorChain configured Fjwt claims chain
   * @return the access token customizer
   */
  @Bean
  @ConditionalOnMissingBean(OAuth2TokenCustomizer.class)
  public OAuth2TokenCustomizer<JwtEncodingContext> fjwtOAuth2TokenCustomizer(
      FjwtClaimsExtractorChain claimsExtractorChain) {

    return context -> {
      if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())
          && context.getPrincipal().getPrincipal() instanceof UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>(claimsExtractorChain.getClaims(userDetails));
        RESERVED_ACCESS_TOKEN_CLAIMS.forEach(claims::remove);
        context.getClaims().claims(existingClaims -> existingClaims.putAll(claims));
      }
    };
  }

  /**
   * Registers the filter chain for OAuth 2.0 and OpenID Connect protocol endpoints.
   *
   * @param httpSecurity HTTP security builder
   * @param registeredClientRepository registered OAuth clients
   * @param authorizationServerSettings authorization server endpoint settings
   * @param authenticationConverter JWT authentication converter
   * @return the protocol endpoint filter chain
   */
  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public SecurityFilterChain fjwtAuthorizationServerSecurityFilterChain(
      HttpSecurity httpSecurity,
      RegisteredClientRepository registeredClientRepository,
      AuthorizationServerSettings authorizationServerSettings,
      FjwtOAuth2JwtAuthenticationConverter authenticationConverter) {

    log.debug(
        "configuring Fjwt as an OAuth 2.0 Authorization Server with OIDC [{}]",
        fjwtProperties.getOauth2().isOidcEnabled());

    httpSecurity.oauth2AuthorizationServer(
        authorizationServer -> {
          httpSecurity.securityMatcher(authorizationServer.getEndpointsMatcher());
          if (fjwtProperties.getOauth2().isOidcEnabled()) {
            authorizationServer.oidc(Customizer.withDefaults());
          }
        });
    httpSecurity
        .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
        .oauth2ResourceServer(
            resourceServer ->
                resourceServer.jwt(jwt -> jwt.jwtAuthenticationConverter(authenticationConverter)))
        .exceptionHandling(
            exceptions ->
                exceptions.defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/login"), htmlRequestMatcher()));

    return httpSecurity.build();
  }

  /**
   * Registers the application and resource-owner login filter chain.
   *
   * @param httpSecurity HTTP security builder
   * @param authenticationConverter JWT authentication converter
   * @return the application filter chain
   */
  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE + 1)
  public SecurityFilterChain fjwtAuthorizationServerApplicationSecurityFilterChain(
      HttpSecurity httpSecurity, FjwtOAuth2JwtAuthenticationConverter authenticationConverter) {

    httpSecurity
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(fjwtProperties.getUnsecured().toArray(String[]::new))
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .formLogin(Customizer.withDefaults())
        .oauth2ResourceServer(
            resourceServer ->
                resourceServer.jwt(jwt -> jwt.jwtAuthenticationConverter(authenticationConverter)))
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

  private RequestMatcher htmlRequestMatcher() {
    MediaTypeRequestMatcher requestMatcher = new MediaTypeRequestMatcher(MediaType.TEXT_HTML);
    requestMatcher.setIgnoredMediaTypes(Set.of(MediaType.ALL));
    return requestMatcher;
  }
}
