package payment.system.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import payment.system.app.entity.User;
import payment.system.app.repository.UserRepository;

@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    CustomUserDetailsService.class);

    private final UserRepository userRepository;

    public CustomUserDetailsService(
            UserRepository userRepository) {

        if (userRepository == null) {

            logger.error("UserRepository bean is null");

            throw new IllegalArgumentException(
                    "UserRepository cannot be null");
        }

        this.userRepository = userRepository;

        logger.info(
                "CustomUserDetailsService initialized successfully");
    }

    @Override
    public UserDetails loadUserByUsername(
            String email) {

        logger.info(
                "Loading user by email : {}",
                email);

        if (email == null || email.isBlank()) {

            logger.error(
                    "Email is null or empty");

            throw new IllegalArgumentException(
                    "Email cannot be null or empty");
        }

        try {

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {

                        logger.error(
                                "User not found with email : {}",
                                email);

                        return new UsernameNotFoundException(
                                "User not found with email : "
                                        + email);
                    });

            logger.info(
                    "User found successfully with email : {}",
                    email);

            UserDetails userDetails =
                    new CustomUserDetails(user);

            logger.info(
                    "CustomUserDetails created successfully for email : {}",
                    email);

            return userDetails;

        } catch (UsernameNotFoundException ex) {

            logger.error(
                    "UsernameNotFoundException occurred for email : {}",
                    email,
                    ex);

            throw ex;

        } catch (Exception ex) {

            logger.error(
                    "Unexpected error occurred while loading user by email : {}",
                    email,
                    ex);

            throw new RuntimeException(
                    "Failed to load user details",
                    ex);
        }
    }
}