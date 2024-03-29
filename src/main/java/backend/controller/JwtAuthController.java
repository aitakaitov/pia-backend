package backend.controller;

import backend.auth.requests.JwtRequest;
import backend.auth.responses.JwtResponse;
import backend.auth.JwtTokenUtil;
import backend.auth.JwtUserDetailsService;

import backend.auth.responses.UserRole;
import backend.constants.Constants;
import backend.model.repo.RoleRepository;
import backend.model.repo.UserRepository;
import lombok.RequiredArgsConstructor;
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
import java.util.HashMap;

@CrossOrigin
@RestController
@Slf4j
@RequiredArgsConstructor
public class JwtAuthController {

    private final HashMap<String, String> emailTokenMap;

    private final AuthenticationManager authenticationManager;

    private final JwtTokenUtil jwtTokenUtil;

    private final JwtUserDetailsService userDetailsService;

    private final RoleRepository roleRepository;

    private final UserRepository userRepository;

    @RequestMapping(value = "/api/auth/authentication", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {

        String username = authenticationRequest.getUsername();
        String password = authenticationRequest.getPassword();

        authenticate(username, password);

        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(username);

        final String token = jwtTokenUtil.generateToken(userDetails);

        emailTokenMap.put(username, token);

        String role = getRole(username);
        if (role.equals(Constants.ADMIN_ROLE_NAME)) {
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

    private String getRole(String email) {
        var user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return null;
        }

        var roleAdmin = roleRepository.getRoleByName(Constants.ADMIN_ROLE_NAME);

        if (roleAdmin.isPresent()) {
            if (user.get().getRoles().contains(roleAdmin.get())){
                return Constants.ADMIN_ROLE_NAME;
            } else {
                return Constants.USER_ROLE_NAME;
            }
        }

        return null;
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

        emailTokenMap.put(username, newToken);

        String role = getRole(username);
        if (role.equals("ROLE_" + Constants.ADMIN_ROLE_NAME)) {
            return ResponseEntity.ok(new JwtResponse(newToken, UserRole.ADMIN));
        }
        else {
            return ResponseEntity.ok(new JwtResponse(newToken, UserRole.USER));
        }
    }

}
