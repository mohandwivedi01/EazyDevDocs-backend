package net.backend.journalApp.jwtFilter;
import lombok.extern.slf4j.Slf4j;
import net.backend.journalApp.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component // Marks this class as a Spring component to be managed by Spring's context.
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsService userDetailsService; // Service to load user details from the database.

    @Autowired
    private JwtUtil jwtUtil; // Utility class to handle JWT (JSON Web Token) operations.

    /**
     * This method intercepts the request to check the presence and validity of a JWT token.
     * If the token is valid, it sets the authentication in the security context.
     *
     * @param request The HTTP request.
     * @param response The HTTP response.
     * @param chain The filter chain.
     * @throws ServletException If an error occurs during request processing.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        // Get the "Authorization" header from the request.
        String authorizationHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        // If the Authorization header is present and starts with "Bearer ", extract the token.
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // Extract the token (after "Bearer ").
            username = jwtUtil.extractUsername(jwt); // Extract the username from the token.
            log.info("JWT token detected in request. Extracted username: {}", username);
        }else {
            log.warn("No JWT token found in request or token format is incorrect.");
        }

        // If a username is extracted from the token, validate it and set the authentication context.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) { // make changes*****************
            log.info("Validating JWT token for user: {}", username);

            UserDetails userDetails = userDetailsService.loadUserByUsername(username); // Load the user details using the username.

            // If the JWT token is valid, authenticate the user.
            if (jwtUtil.validateToken(jwt, userDetails)) {
                log.info("JWT token validated successfully for user: {}", username);

                // Create an authentication token for the user with the authorities/roles.
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); // Set additional request details.

                // Set the authentication in the SecurityContext so that it can be accessed later in the request cycle.
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.info("User '{}' is now authenticated.", username);
            } else {
                log.warn("Invalid JWT token for user: {}", username);
            }
        }

        // Proceed with the request handling by passing control to the next filter in the chain.
        log.debug("Proceeding with request processing.");
        chain.doFilter(request, response);
    }
}


