package com.enginious.fjwt.core;

import com.enginious.fjwt.dto.FjwtRequest;
import com.enginious.fjwt.dto.FjwtResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequiredArgsConstructor
public class FjwtController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final FjwtTokenUtil fjwtTokenUtil;

    @RequestMapping(value = "${fjwt.endpoint:/authenticate}", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody FjwtRequest request) throws Exception {

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            return ResponseEntity
                    .ok(
                            FjwtResponse
                                    .builder()
                                    .token(fjwtTokenUtil.generateToken(userDetailsService.loadUserByUsername(request.getUsername())))
                                    .build());

        } catch (AuthenticationException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }
    }
}
