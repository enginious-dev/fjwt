package com.enginious.fjwt.core;

import com.enginious.fjwt.dto.FjwtRequest;
import com.enginious.fjwt.dto.FjwtResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequiredArgsConstructor
public class FjwtController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final FjwtTokenUtil fjwtTokenUtil;

    @RequestMapping(value = "${fjwt.url:/authenticate}", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody FjwtRequest request) throws Exception {
        authenticate(request.getUsername(), request.getPassword());

        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

        final String token = fjwtTokenUtil.generateToken(userDetails);

        return ResponseEntity.ok(FjwtResponse.builder().token(token).build());
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}
