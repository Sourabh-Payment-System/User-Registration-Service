package payment.system.app.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import payment.system.app.entity.User;

public class CustomUserDetails implements UserDetails {

    private static final Logger logger =
            LoggerFactory.getLogger(CustomUserDetails.class);

    private final User user;

    public CustomUserDetails(User user) {

        if (user == null) {

            logger.error("User object is null");

            throw new IllegalArgumentException(
                    "User cannot be null");
        }

        this.user = user;

        logger.info(
                "CustomUserDetails initialized for user : {}",
                user.getEmail());
    }

    @Override
    public Collection<? extends GrantedAuthority>
    getAuthorities() {

        logger.info(
                "Fetching authorities for user : {}",
                user.getEmail());

        Set<SimpleGrantedAuthority> authorities =
                new HashSet<>();

        if (user.getRoles() == null
                || user.getRoles().isEmpty()) {

            logger.warn(
                    "No roles assigned to user : {}",
                    user.getEmail());

            return authorities;
        }

        try {

            user.getRoles().forEach(role -> {

                if (role == null) {

                    logger.warn(
                            "Null role found for user : {}",
                            user.getEmail());

                    return;
                }

                logger.info(
                        "Adding role authority : ROLE_{} for user : {}",
                        role.getName(),
                        user.getEmail());

                authorities.add(
                        new SimpleGrantedAuthority(
                                "ROLE_" + role.getName()));

                if (role.getPermissions() == null
                        || role.getPermissions().isEmpty()) {

                    logger.warn(
                            "No permissions found for role : {}",
                            role.getName());

                    return;
                }

                role.getPermissions().forEach(permission -> {

                    if (permission == null) {

                        logger.warn(
                                "Null permission found in role : {}",
                                role.getName());

                        return;
                    }

                    logger.info(
                            "Adding permission : {} for user : {}",
                            permission.getName(),
                            user.getEmail());

                    authorities.add(
                            new SimpleGrantedAuthority(
                                    permission.getName()));
                });
            });

            logger.info(
                    "Successfully fetched {} authorities for user : {}",
                    authorities.size(),
                    user.getEmail());

            return authorities;

        } catch (Exception ex) {

            logger.error(
                    "Error while fetching authorities for user : {}",
                    user.getEmail(),
                    ex);

            throw new RuntimeException(
                    "Failed to fetch user authorities",
                    ex);
        }
    }

    @Override
    public String getUsername() {

        logger.debug(
                "Fetching username for user");

        String email = user.getEmail();

        if (email == null || email.isBlank()) {

            logger.error(
                    "User email is null or empty");

            throw new IllegalArgumentException(
                    "User email cannot be null or empty");
        }

        return email;
    }

    @Override
    public String getPassword() {

        logger.debug(
                "Fetching password for user : {}",
                user.getEmail());

        String password = user.getPassword();

        if (password == null || password.isBlank()) {

            logger.error(
                    "Password is null or empty for user : {}",
                    user.getEmail());

            throw new IllegalArgumentException(
                    "Password cannot be null or empty");
        }

        return password;
    }

    @Override
    public boolean isAccountNonExpired() {

        logger.debug(
                "Checking account expiration status for user : {}",
                user.getEmail());

        return true;
    }

    @Override
    public boolean isAccountNonLocked() {

        logger.debug(
                "Checking account lock status for user : {}",
                user.getEmail());

        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {

        logger.debug(
                "Checking credentials expiration status for user : {}",
                user.getEmail());

        return true;
    }

    @Override
    public boolean isEnabled() {

        logger.debug(
                "Checking enabled status for user : {}",
                user.getEmail());

        return true;
    }
}