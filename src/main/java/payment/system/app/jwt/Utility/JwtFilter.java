package payment.system.app.jwt.Utility;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import payment.system.app.dto.ErrorMessageDto;
import payment.system.app.service.CustomUserDetailsService;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger =
            LoggerFactory.getLogger(JwtFilter.class);

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtFilter(
            JwtUtil jwtUtil,
            CustomUserDetailsService userDetailsService) {

        if (jwtUtil == null) {

            logger.error("JwtUtil bean is null");

            throw new IllegalArgumentException(
                    "JwtUtil cannot be null");
        }

        if (userDetailsService == null) {

            logger.error("CustomUserDetailsService bean is null");

            throw new IllegalArgumentException(
                    "CustomUserDetailsService cannot be null");
        }

        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;

        logger.info("JwtFilter initialized successfully");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        logger.info(
                "Processing request for path : {}",
                path);

        // Skip JWT validation for public endpoints
        if (path.startsWith("/auth/")
                || path.equals("/users/register")) {

            logger.info(
                    "Skipping JWT validation for public endpoint : {}",
                    path);

            filterChain.doFilter(request, response);
            return;
        }

        String authHeader =
                request.getHeader("Authorization");

        if (authHeader == null
                || !authHeader.startsWith("Bearer ")) {

            logger.warn(
                    "Authorization header missing or invalid for path : {}",
                    path);

            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {

            logger.info("Extracting username from JWT token");

            String username =
                    jwtUtil.extractUsername(token);

            if (username == null
                    || username.isBlank()) {

                logger.error(
                        "Username extraction failed from JWT token");

                throw new IllegalArgumentException(
                        "Invalid JWT token");
            }

            logger.info(
                    "JWT token belongs to user : {}",
                    username);

            if (SecurityContextHolder.getContext()
                    .getAuthentication() == null) {

                logger.info(
                        "Loading user details for username : {}",
                        username);

                UserDetails userDetails =
                        userDetailsService
                                .loadUserByUsername(username);

                if (userDetails == null) {

                    logger.error(
                            "UserDetails not found for username : {}",
                            username);

                    throw new UsernameNotFoundException(
                            "User not found");
                }

                boolean isTokenValid =
                        jwtUtil.validateToken(
                                token,
                                userDetails.getUsername());

                if (!isTokenValid) {

                    logger.error(
                            "JWT token validation failed for username : {}",
                            username);

                    throw new IllegalArgumentException(
                            "JWT token is invalid or expired");
                }

                UsernamePasswordAuthenticationToken
                        authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request));

                // Set authenticated user into Security Context
                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);

                logger.info(
                        "User authenticated successfully : {}",
                        username);
            }

        } catch (Exception ex) {

            logger.error(
                    "JWT authentication failed : {}",
                    ex.getMessage(),
                    ex);

            SecurityContextHolder.clearContext();

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED);

            response.setContentType(
                    MediaType.APPLICATION_JSON_VALUE);

            ErrorMessageDto error =
                    new ErrorMessageDto(
                            java.time.LocalDateTime.now(),
                            HttpServletResponse.SC_UNAUTHORIZED,
                            "Unauthorized",
                            ex.getMessage());

            ObjectMapper mapper = new ObjectMapper();

            response.getWriter().write(
                    mapper.writeValueAsString(error));

            return;
        }

        filterChain.doFilter(request, response);
    }
}