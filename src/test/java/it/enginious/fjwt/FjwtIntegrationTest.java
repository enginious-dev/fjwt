package it.enginious.fjwt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnableAutoConfiguration
@TestPropertySource(
        properties = {
                "fjwt.algorithm=HS256",
                "fjwt.secret=mZq4t7w!z%C*F)J@NcRfUjXn2r5u8x/A",
                "fjwt.unsecured[0]=/unsecuredEndpoint",
                "spring.main.allow-bean-definition-overriding=true"
        })
class FjwtIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenRequestOnSecureEnpointAndTokenIsNotPresentShouldReturn401() throws Exception {

        mockMvc.perform(get("/securedEndpoint")).andExpect(status().isUnauthorized());
    }

    @Test
    void whenRequestOnUnsecureEnpointAndTokenIsNotPresentShouldReturn200() throws Exception {

        mockMvc.perform(get("/unsecuredEndpoint")).andExpect(status().isOk());
    }

    @Test
    void whenRequestOnSecureEndpointAndValidTokenIsPresentAndValidShouldReturn200() throws Exception {

        mockMvc
                .perform(
                        get("/securedEndpoint")
                                .header(
                                        "Authorization",
                                        "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VybmFtZSIsImV4cCI6MTYzNTM0MzIwMCwiaWF0IjoxNjM1MzM5NjAwLCJjcmVkZW50aWFsc0V4cGlyZWQiOmZhbHNlLCJhY2NvdW50RXhwaXJlZCI6ZmFsc2UsImFjY291bnRMb2NrZWQiOmZhbHNlLCJlbmFibGVkIjp0cnVlfQ.UQc4HYZAt3phnlLmX3ASC8yQAx3H4bqO0BYYg2QR2GU"))
                .andExpect(status().isOk());
    }

    @Test
    void whenRequestOnSecureEndpointAndValidTokenIsPresentAndExpiredShouldReturn401()
            throws Exception {

        mockMvc
                .perform(
                        get("/securedEndpoint")
                                .header(
                                        "Authorization",
                                        "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VybmFtZSIsImV4cCI6MTYzMDAwMzIwMCwiaWF0IjoxNjMwMDA5NjAwfQ.vWDe3Vj3omE36GQUqJYwGX-ZWwp4IWRJ3Eg8qivehM4"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenRequestOnSecureEndpointAndValidTokenIsPresentAndHasInvalidSignatureShouldReturn401()
            throws Exception {
        mockMvc
                .perform(
                        get("/securedEndpoint")
                                .header(
                                        "Authorization",
                                        "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VybmFtZSIsImV4cCI6MTYzMDAwMzIwMCwiaWF0IjoxNjMwMDA5NjAwfQ.aechoT_1XYVm1GNdqUj9C4y3b7bqzcUj0mo-xeQAIjA"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenAuthenticateAndUsernameIsBlankShouldReturn400() throws Exception {
        mockMvc
                .perform(
                        post("/authenticate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenAuthenticateAndUsernameIsNotBlankShouldReturnToken() throws Exception {
        mockMvc
                .perform(
                        post("/authenticate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\": \"username\", \"password\": \"username\"}"))
                .andExpect(status().isOk());
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
