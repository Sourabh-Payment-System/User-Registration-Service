package payment.system.app.config;

import java.io.IOException;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.MediaType;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import payment.system.app.dto.ErrorMessageDto;

@Component
public class CustomAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    CustomAuthenticationEntryPoint.class);

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException, ServletException {

        logger.error(
                "Unauthorized access error : {}",
                authException.getMessage());

        ErrorMessageDto error =
                new ErrorMessageDto(
                        LocalDateTime.now(),
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Unauthorized",
                        "Authentication is required to access this resource"
                );

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED);

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE);

        ObjectMapper mapper = new ObjectMapper();

        /**
         * Register JavaTimeModule
         */
        mapper.findAndRegisterModules();

        response.getWriter().write(
                mapper.writeValueAsString(error));
    }
}