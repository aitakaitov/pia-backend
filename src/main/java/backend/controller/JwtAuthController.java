package backend.controller;

import backend.auth.JwtRequest;
import backend.auth.JwtResponse;
import backend.auth.JwtTokenUtil;
import backend.auth.JwtUserDetailsService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
@Slf4j
public class JwtAuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @RequestMapping(value = "/auth/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {

        String username = authenticationRequest.getUsername();
        String password = authenticationRequest.getPassword();

        authenticate(username, password);

        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(username);

        final String token = jwtTokenUtil.generateToken(userDetails);

        return ResponseEntity.ok(new JwtResponse(token));
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

    @RequestMapping(value = "/auth/refresh", method = RequestMethod.POST)
    public ResponseEntity<?> refreshAuthenticationToken(@RequestHeader(name = "Authorization") String authHeader) throws Exception {

        // Here we are already authenticated, since only /auth/authenticate is excluded from the process
        // thus we only need to extract the email and generate new token

        if (authHeader == null || authHeader.length() == 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        log.info("TEST ---- "  + SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString());

        String token = jwtTokenUtil.getTokenFromHeader(authHeader);
        String username = jwtTokenUtil.getUsernameFromToken(token);

        // Generate new token and update it
        String newToken = jwtTokenUtil.generateToken(User.builder().username(username).password("").authorities(Collections.emptySet()).build());

        return ResponseEntity.ok(new JwtResponse(newToken));
    }

}
