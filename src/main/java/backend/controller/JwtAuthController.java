package backend.controller;

import backend.auth.requests.JwtRequest;
import backend.auth.responses.JwtResponse;
import backend.auth.JwtTokenUtil;
import backend.auth.JwtUserDetailsService;

import backend.auth.responses.UserRole;
import backend.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

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

    @RequestMapping(value = "/api/auth/authentication", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {

        String username = authenticationRequest.getUsername();
        String password = authenticationRequest.getPassword();

        authenticate(username, password);

        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(username);

        final String token = jwtTokenUtil.generateToken(userDetails);

        String role = getRole();
        if (role.equals("ROLE_" + Constants.ADMIN_ROLE_NAME)) {
            return ResponseEntity.ok(new JwtResponse(token, UserRole.ADMIN));
        }
        else {
            return ResponseEntity.ok(new JwtResponse(token, UserRole.USER));
        }
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

    private String getRole() {
        // This is a bit weird, but we know only one authority is granted to a user right now
        // and that it is a SimpleGrantedAuthority, so we can do this. If there were more authorities, we would have to
        // do it another way
        String role = "";
        for (var sga : SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
            if (sga instanceof SimpleGrantedAuthority) {
                role = sga.getAuthority();
            }
        }

        return role;
    }

    @RequestMapping(value = "/api/auth/refresh", method = RequestMethod.GET)
    public ResponseEntity<?> refreshAuthenticationToken(@RequestHeader(name = "Authorization") String authHeader) throws Exception {

        // Here we are already authenticated, since only /auth/authenticate is excluded from the process
        // thus we only need to extract the email and generate new token

        if (authHeader == null || authHeader.length() == 0) {
            log.info("Refresh endpoint received request without auth header");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Authorization header is not present");
        }

        String token = jwtTokenUtil.getTokenFromHeader(authHeader);
        String username = jwtTokenUtil.getUsernameFromToken(token);

        // Generate new token and update it
        String newToken = jwtTokenUtil.generateToken(User.builder().username(username).password("").authorities(Collections.emptySet()).build());

        String role = getRole();
        if (role.equals("ROLE_" + Constants.ADMIN_ROLE_NAME)) {
            return ResponseEntity.ok(new JwtResponse(newToken, UserRole.ADMIN));
        }
        else {
            return ResponseEntity.ok(new JwtResponse(newToken, UserRole.USER));
        }
    }

}
