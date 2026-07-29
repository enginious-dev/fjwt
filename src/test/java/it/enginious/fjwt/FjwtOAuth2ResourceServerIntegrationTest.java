package it.enginious.fjwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import it.enginious.fjwt.core.FjwtController;
import it.enginious.fjwt.core.FjwtRequestFilter;
import it.enginious.fjwt.core.FjwtTokenUtil;

@SpringBootTest
@AutoConfigureMockMvc
@EnableAutoConfiguration
@TestPropertySource(
    properties = {
      "fjwt.security-mode=OAUTH2_RESOURCE_SERVER",
      "fjwt.unsecured[0]=/oauth2/unsecured"
    })
class FjwtOAuth2ResourceServerIntegrationTest {

  @Autowired private ApplicationContext applicationContext;
  @Autowired private MockMvc mockMvc;

  @Test
  void shouldNotRegisterLegacyAuthenticationComponents() {
    assertThat(applicationContext.getBeansOfType(FjwtController.class)).isEmpty();
    assertThat(applicationContext.getBeansOfType(FjwtRequestFilter.class)).isEmpty();
    assertThat(applicationContext.getBeansOfType(FjwtTokenUtil.class)).isEmpty();
  }

  @Test
  void shouldRejectRequestWithoutAccessToken() throws Exception {
    mockMvc.perform(get("/oauth2/secured")).andExpect(status().isUnauthorized());
  }

  @Test
  void shouldAllowConfiguredUnsecuredRequest() throws Exception {
    mockMvc.perform(get("/oauth2/unsecured")).andExpect(status().isOk());
  }

  @Test
  void shouldAuthenticateScopesAndApplicationAuthorities() throws Exception {
    mockMvc
        .perform(
            get("/oauth2/secured").header(HttpHeaders.AUTHORIZATION, "Bearer valid-access-token"))
        .andExpect(status().isOk());
  }

  @Configuration
  static class ResourceServerTestConfig {

    @Bean
    JwtDecoder jwtDecoder() {
      return token ->
          Jwt.withTokenValue(token)
              .header("alg", "RS256")
              .subject("alice")
              .issuedAt(Instant.now())
              .expiresAt(Instant.now().plusSeconds(60))
              .claim("scope", "api.read")
              .claim("authorities", List.of("ROLE_ADMIN"))
              .build();
    }

    @Bean
    OAuth2TestEndpoint oauth2TestEndpoint() {
      return new OAuth2TestEndpoint();
    }
  }

  @RestController
  static class OAuth2TestEndpoint {

    @GetMapping("/oauth2/secured")
    @PreAuthorize("hasAuthority('SCOPE_api.read') && hasAuthority('ROLE_ADMIN')")
    ResponseEntity<String> secured() {
      return ResponseEntity.ok("secured");
    }

    @GetMapping("/oauth2/unsecured")
    ResponseEntity<String> unsecured() {
      return ResponseEntity.ok("unsecured");
    }
  }
}
