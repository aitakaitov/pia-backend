package backend.auth;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import backend.constants.Constants;
import io.jsonwebtoken.MalformedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;

import java.io.IOException;
import java.util.HashMap;

@Component
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private HashMap<String, String> emailTokenMap;

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");
        log.error(requestTokenHeader);

        String username = null;
        String jwtToken = null;

        // We authorize using Bearer, so we remove it and process the following token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                log.info("Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                log.info("JWT Token has expired");
            } catch (MalformedJwtException e) {
                log.info("Received malformed JWT token");
            }
        } else {
            log.warn("JWT Token does not begin with Bearer String");
        }

        // validate the token
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(username);

            // If the token is valid + is set in the map as the user's current token, authenticate
            if (jwtTokenUtil.validateToken(jwtToken, userDetails)
                    && (emailTokenMap.containsKey(username) && emailTokenMap.get(username).equals(jwtToken))) {

                // Set authentication in the context so that we can refer to it later on
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Specify the current user is authenticated in the session
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                getServletContext().setAttribute(Constants.SCONTEXT_USER_EMAIL_KEY, userDetails.getUsername());
            }
        }
        chain.doFilter(request, response);
    }

}
