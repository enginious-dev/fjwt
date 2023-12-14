package it.enginious.fjwt.core;

import it.enginious.fjwt.core.exceptions.FjwtTokenInvalidatorException;
import it.enginious.fjwt.dto.FjwtRequest;
import it.enginious.fjwt.dto.FjwtResponse;
import jakarta.validation.Valid;
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
 * @author Giuseppe Milazzo
 * @since 1.0.0
 */
@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
public class FjwtController {

    private final AuthenticationManager authenticationManager;
    private final FjwtTokenUtil fjwtTokenUtil;
    private final FjwtTokenInvalidator fjwtTokenInvalidator;

    /**
     * Authentication endpoint, you can set this path through {@link FjwtConfig#endpoint}.
     *
     * @param request a {@link FjwtRequest}
     * @return the authentication response which is {@link HttpStatus#OK} in case of success and
     * {@link HttpStatus#UNAUTHORIZED} in case of failure.
     */
    @PostMapping("${fjwt.endpoint:/authenticate}")
    public ResponseEntity<FjwtResponse> createAuthenticationToken(
            @Valid @RequestBody FjwtRequest request) {

        try {
            log.debug("processing request for user [{}]", request.getUsername());

            UserDetails user =
                    (UserDetails)
                            (authenticationManager.authenticate(
                                    new UsernamePasswordAuthenticationToken(
                                            request.getUsername(), request.getPassword())))
                                    .getPrincipal();

            String token = fjwtTokenUtil.generateToken(user);

            if (!(fjwtTokenInvalidator instanceof NoopTokenInvalidator)) {
                log.debug("storing token for user [{}]", request.getUsername());
                fjwtTokenInvalidator.store(user, token);
            }

            return ResponseEntity.ok(FjwtResponse.builder().token(token).build());

        } catch (AuthenticationException e) {
            log.error(
                    String.format(
                            "error occurred while processing request for user [%s]:", request.getUsername()),
                    e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (FjwtTokenInvalidatorException e) {
            log.error(
                    String.format("error occurred while storing token for user [%s]", request.getUsername()),
                    e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
