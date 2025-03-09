//package net.backend.journalApp.config;
//
//import net.backend.journalApp.jwtFilter.JwtFilter;
//import net.backend.journalApp.services.UserDetailsServiceImpl;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
///**
// * SecurityConfig class is responsible for configuring the security aspects of the application.
// * It customizes user authentication, authorization rules, password encoding, and integrates JWT-based security.
// */
//@Configuration // Marks this class as a source of configuration.
//@EnableWebSecurity // Enables Spring Security in the application.
//public class SecurityConfig extends WebSecurityConfigurerAdapter {
//
//    @Autowired
//    private UserDetailsServiceImpl userDetailsService; // Service to load user details for authentication.
//
//    @Autowired
//    private JwtFilter jwtFilter; // Custom JWT filter to validate tokens in incoming requests.
//
//
//
//    /**
//     * Configures HTTP security, defining which endpoints require authentication or specific roles.
//     * Integrates the custom JWT filter and disables CSRF as JWT is stateless.
//     */
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http.authorizeHttpRequests()
//                // Secures journal and user endpoints to require authentication.
//                .antMatchers("/journal/**", "/user/**").authenticated()
//                // Restricts access to admin endpoints to users with the ADMIN role.
//                .antMatchers("/admin/**").hasRole("ADMIN")
//                // Allows unrestricted access to all other endpoints.
//                .anyRequest().permitAll()
//                .and()
//                // Disables session management for stateless JWT authentication.
//                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                .and()
//                // Disables CSRF protection since JWT is used.
//                .csrf().disable();
//        // Adds the custom JWT filter to validate tokens before the username/password authentication filter.
//        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
//    }
//
//    /**
//     * Configures the AuthenticationManager to use a custom user details service and a password encoder.
//     */
//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.userDetailsService(userDetailsService)
//                .passwordEncoder(passwordEncoder()); // Configures password encoding with BCrypt.
//    }
//
//    /**
//     * Creates a bean for the PasswordEncoder using BCrypt hashing algorithm.
//     * This ensures passwords are stored securely in the database.
//     *
//     * @return PasswordEncoder instance.
//     */
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    /**
//     * Exposes the AuthenticationManager as a Spring bean to allow its use in other parts of the application.
//     *
//     * @return AuthenticationManager instance.
//     */
//    @Bean
//    @Override
//    public AuthenticationManager authenticationManagerBean() throws Exception {
//        return super.authenticationManagerBean();
//    }
//}





package net.backend.journalApp.config;

import net.backend.journalApp.jwtFilter.JwtFilter;
import net.backend.journalApp.services.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;

/**
 * SecurityConfig class is responsible for configuring authentication, authorization, and JWT security.
 */
@Configuration
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /**
     * Defines the security filter chain to manage authentication and authorization.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors().and()  // Enable CORS if frontend is separate
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/api/v1/login", "/api/v1/signup").permitAll()
                .antMatchers("/api/v1/journal/**", "/api/v1/user/**").authenticated()
                .antMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().permitAll()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // JWT is stateless
                .and()
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Creates a bean for password encoding using BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Defines the authentication provider, linking it with the user details service and password encoder.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Defines the authentication manager using the custom authentication provider.
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(authenticationProvider());
    }
}
