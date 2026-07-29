package it.enginious.fjwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import it.enginious.fjwt.core.FjwtController;
import it.enginious.fjwt.core.FjwtRequestFilter;
import it.enginious.fjwt.core.FjwtTokenUtil;

@SpringBootTest
@AutoConfigureMockMvc
@EnableAutoConfiguration
@TestPropertySource(
    properties = {
      "fjwt.security-mode=OAUTH2_AUTHORIZATION_SERVER",
      "spring.security.oauth2.authorizationserver.issuer=http://localhost",
      "spring.security.oauth2.authorizationserver.client.test.registration.client-id=test-client",
      "spring.security.oauth2.authorizationserver.client.test.registration.client-secret={noop}secret",
      "spring.security.oauth2.authorizationserver.client.test.registration.client-authentication-methods[0]=client_secret_basic",
      "spring.security.oauth2.authorizationserver.client.test.registration.authorization-grant-types[0]=client_credentials",
      "spring.security.oauth2.authorizationserver.client.test.registration.scopes[0]=api.read"
    })
class FjwtOAuth2AuthorizationServerIntegrationTest {

  @Autowired private ApplicationContext applicationContext;
  @Autowired private MockMvc mockMvc;

  @Test
  void shouldNotRegisterLegacyAuthenticationComponents() {
    assertThat(applicationContext.getBeansOfType(FjwtController.class)).isEmpty();
    assertThat(applicationContext.getBeansOfType(FjwtRequestFilter.class)).isEmpty();
    assertThat(applicationContext.getBeansOfType(FjwtTokenUtil.class)).isEmpty();
  }

  @Test
  void shouldExposeAuthorizationServerMetadataAndJwkSet() throws Exception {
    mockMvc
        .perform(get("/.well-known/oauth-authorization-server"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.issuer").value("http://localhost"))
        .andExpect(jsonPath("$.token_endpoint").value("http://localhost/oauth2/token"))
        .andExpect(jsonPath("$.jwks_uri").value("http://localhost/oauth2/jwks"));

    mockMvc
        .perform(get("/oauth2/jwks"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
        .andExpect(jsonPath("$.keys[0].kid").isNotEmpty());

    mockMvc
        .perform(get("/.well-known/openid-configuration"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.issuer").value("http://localhost"));
  }

  @Test
  void shouldIssueClientCredentialsAccessToken() throws Exception {
    String credentials =
        Base64.getEncoder().encodeToString("test-client:secret".getBytes(StandardCharsets.UTF_8));

    mockMvc
        .perform(
            post("/oauth2/token")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("grant_type", "client_credentials")
                .param("scope", "api.read"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.access_token").isNotEmpty())
        .andExpect(jsonPath("$.token_type").value("Bearer"))
        .andExpect(jsonPath("$.scope").value("api.read"));
  }

  @Test
  void shouldRejectResourceOwnerPasswordGrant() throws Exception {
    String credentials =
        Base64.getEncoder().encodeToString("test-client:secret".getBytes(StandardCharsets.UTF_8));

    mockMvc
        .perform(
            post("/oauth2/token")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("grant_type", "password")
                .param("username", "alice")
                .param("password", "password"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("unsupported_grant_type"));
  }

  @Configuration
  static class AuthorizationServerTestConfig {}
}
