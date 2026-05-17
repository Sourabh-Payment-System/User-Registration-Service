package payment.system.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import payment.system.app.jwt.Utility.JwtFilter;
import payment.system.app.service.CustomUserDetailsService;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    SecurityConfig.class);

    private final JwtFilter jwtFilter;

    private final CustomAuthenticationEntryPoint
            authenticationEntryPoint;

    private final CustomAccessDeniedHandler
            accessDeniedHandler;

    public SecurityConfig(
            JwtFilter jwtFilter,
            CustomAuthenticationEntryPoint authenticationEntryPoint,
            CustomAccessDeniedHandler accessDeniedHandler) {

        if (jwtFilter == null) {

            logger.error(
                    "JwtFilter bean is null during SecurityConfig initialization");

            throw new IllegalArgumentException(
                    "JwtFilter cannot be null");
        }

        if (authenticationEntryPoint == null) {

            logger.error(
                    "CustomAuthenticationEntryPoint bean is null");

            throw new IllegalArgumentException(
                    "CustomAuthenticationEntryPoint cannot be null");
        }

        if (accessDeniedHandler == null) {

            logger.error(
                    "CustomAccessDeniedHandler bean is null");

            throw new IllegalArgumentException(
                    "CustomAccessDeniedHandler cannot be null");
        }

        this.jwtFilter = jwtFilter;
        this.authenticationEntryPoint =
                authenticationEntryPoint;
        this.accessDeniedHandler =
                accessDeniedHandler;

        logger.info(
                "SecurityConfig initialized successfully");
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http)
            throws Exception {

        try {

            logger.info(
                    "Configuring SecurityFilterChain");

            http

                /**
                 * Disable CSRF
                 */
                .csrf(csrf -> {

                    logger.info(
                            "Disabling CSRF protection for JWT authentication");

                    csrf.disable();
                })

                /**
                 * Endpoint authorization
                 */
                .authorizeHttpRequests(auth -> auth

                        /**
                         * Public endpoints
                         */
                        .requestMatchers("/auth/**")
                        .permitAll()

                        .requestMatchers("/users/register")
                        .permitAll()

                        /**
                         * Protected endpoints
                         */
                        .anyRequest()
                        .authenticated()
                )

                /**
                 * Exception handling
                 */
                .exceptionHandling(exception -> {

                    logger.info(
                            "Configuring custom authentication and authorization handlers");

                    exception.authenticationEntryPoint(
                            authenticationEntryPoint);

                    exception.accessDeniedHandler(
                            accessDeniedHandler);
                })

                /**
                 * Stateless session
                 */
                .sessionManagement(session -> {

                    logger.info(
                            "Setting SessionCreationPolicy to STATELESS");

                    session.sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS);
                });

            /**
             * JWT Filter
             */
            logger.info(
                    "Adding JwtFilter before UsernamePasswordAuthenticationFilter");

            http.addFilterBefore(
                    jwtFilter,
                    UsernamePasswordAuthenticationFilter.class
            );

            logger.info(
                    "SecurityFilterChain configured successfully");

            return http.build();

        } catch (Exception ex) {

            logger.error(
                    "Exception occurred while configuring SecurityFilterChain : {}",
                    ex.getMessage(),
                    ex);

            throw new AuthenticationServiceException(
                    "Failed to configure Spring Security",
                    ex);
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        logger.info(
                "Creating BCryptPasswordEncoder bean");

        try {

            PasswordEncoder encoder =
                    new BCryptPasswordEncoder();

            logger.info(
                    "BCryptPasswordEncoder bean created successfully");

            return encoder;

        } catch (Exception ex) {

            logger.error(
                    "Error while creating PasswordEncoder bean : {}",
                    ex.getMessage(),
                    ex);

            throw new RuntimeException(
                    "Unable to create PasswordEncoder bean",
                    ex);
        }
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        logger.info(
                "Creating AuthenticationManager bean");

        try {

            if (configuration == null) {

                logger.error(
                        "AuthenticationConfiguration bean is null");

                throw new IllegalArgumentException(
                        "AuthenticationConfiguration cannot be null");
            }

            AuthenticationManager authenticationManager =
                    configuration.getAuthenticationManager();

            logger.info(
                    "AuthenticationManager bean created successfully");

            return authenticationManager;

        } catch (Exception ex) {

            logger.error(
                    "Error while creating AuthenticationManager : {}",
                    ex.getMessage(),
                    ex);

            throw new AuthenticationServiceException(
                    "Failed to create AuthenticationManager",
                    ex);
        }
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        logger.info(
                "Creating DaoAuthenticationProvider bean");

        try {

            if (userDetailsService == null) {

                logger.error(
                        "CustomUserDetailsService bean is null");

                throw new IllegalArgumentException(
                        "CustomUserDetailsService cannot be null");
            }

            if (passwordEncoder == null) {

                logger.error(
                        "PasswordEncoder bean is null");

                throw new IllegalArgumentException(
                        "PasswordEncoder cannot be null");
            }

            DaoAuthenticationProvider authProvider =
                    new DaoAuthenticationProvider();

            authProvider.setUserDetailsService(
                    userDetailsService);

            authProvider.setPasswordEncoder(
                    passwordEncoder);

            logger.info(
                    "DaoAuthenticationProvider configured successfully");

            return authProvider;

        } catch (Exception ex) {

            logger.error(
                    "Error while configuring DaoAuthenticationProvider : {}",
                    ex.getMessage(),
                    ex);

            throw new AuthenticationServiceException(
                    "Failed to configure AuthenticationProvider",
                    ex);
        }
    }
}