package com.enginious.fjwt;

import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.DefaultJwtParser;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@EnableAutoConfiguration
@TestPropertySource(
    properties = {
      "fjwt.algorithm=HS256",
      "fjwt.secret=secret",
      "fjwt.unsecured[0]=/unsecuredEndpoint",
      "spring.main.allow-bean-definition-overriding=true"
    })
class FjwtIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void whenRequestOnSecureEnpointAndTokenIsNotPresentShouldReturn401() throws Exception {

    mockMvc.perform(get("/securedEndpoint")).andExpect(status().isUnauthorized());
  }

  @Test
  void whenRequestOnUnecureEnpointAndTokenIsNotPresentShouldReturn200() throws Exception {

    mockMvc.perform(get("/unsecuredEndpoint")).andExpect(status().isOk());
  }

  @Test
  void whenRequestOnSecureEnpointAndValidTokenIsPresentAndValidShouldReturn200() throws Exception {

    try (MockedStatic<Jwts> mocked = mockStatic(Jwts.class)) {
      mocked
          .when(Jwts::parser)
          .thenReturn(
              new DefaultJwtParser()
                  .setClock(() -> Date.from(Instant.ofEpochMilli(1635339600000L))));

      mockMvc
          .perform(
              get("/securedEndpoint")
                  .header(
                      "Authorization",
                      "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VybmFtZSIsImV4cCI6MTYzNTM0MzIwMCwiaWF0IjoxNjM1MzM5NjAwLCJjcmVkZW50aWFsc0V4cGlyZWQiOmZhbHNlLCJhY2NvdW50RXhwaXJlZCI6ZmFsc2UsImFjY291bnRMb2NrZWQiOmZhbHNlLCJlbmFibGVkIjp0cnVlfQ.Q8VRD8-1KfVchi-xoJrtu3gutIXsJInnhFRBrDMSmvk"))
          .andExpect(status().isOk());
    }
  }

  @Test
  void whenRequestOnSecureEnpointAndValidTokenIsPresentAndExpiredShouldReturn401()
      throws Exception {

    try (MockedStatic<Jwts> mocked = mockStatic(Jwts.class)) {
      mocked
          .when(Jwts::parser)
          .thenReturn(
              new DefaultJwtParser()
                  .setClock(() -> Date.from(Instant.ofEpochMilli(1635339600000L))));

      mockMvc
          .perform(
              get("/securedEndpoint")
                  .header(
                      "Authorization",
                      "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VybmFtZSIsImV4cCI6MTYzMDAwMzIwMCwiaWF0IjoxNjMwMDA5NjAwfQ.miT7GxnGv7fm6l2GZVYqPapZInhLUMh8Q3hXl5tnabM"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Test
  void whenRequestOnSecureEnpointAndValidTokenIsPresentAndHasInvalidSignatureShouldReturn401()
      throws Exception {

    try (MockedStatic<Jwts> mocked = mockStatic(Jwts.class)) {
      mocked
          .when(Jwts::parser)
          .thenReturn(
              new DefaultJwtParser()
                  .setClock(() -> Date.from(Instant.ofEpochMilli(1635339600000L))));

      mockMvc
          .perform(
              get("/securedEndpoint")
                  .header(
                      "Authorization",
                      "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VybmFtZSIsImV4cCI6MTYzMDAwMzIwMCwiaWF0IjoxNjMwMDA5NjAwfQ.aechoT_1XYVm1GNdqUj9C4y3b7bqzcUj0mo-xeQAIjA"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Configuration
  public static class FjwtITConfig {

    @Bean
    public Clock testClock() {

      return Clock.fixed(Instant.ofEpochMilli(1635339600000L), ZoneId.systemDefault());
    }

    @Bean
    public SecuredEndpoint securedEndpoint() {

      return new SecuredEndpoint();
    }
  }

  @RestController
  public static class SecuredEndpoint {

    @GetMapping("/securedEndpoint")
    public ResponseEntity<String> securedEndpoint() {

      return ResponseEntity.ok("securedEndpoint success");
    }

    @GetMapping("/unsecuredEndpoint")
    public ResponseEntity<String> unsecuredEndpoint() {

      return ResponseEntity.ok("unsecuredEndpoint success");
    }
  }
}
