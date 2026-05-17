package payment.system.app.config;

import java.io.IOException;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import payment.system.app.dto.ErrorMessageDto;

@Component
public class CustomAccessDeniedHandler
        implements AccessDeniedHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    CustomAccessDeniedHandler.class);

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        logger.error(
                "Access denied error : {}",
                accessDeniedException.getMessage());

        ErrorMessageDto error =
                new ErrorMessageDto(
                        LocalDateTime.now(),
                        HttpServletResponse.SC_FORBIDDEN,
                        "Forbidden",
                        "You do not have permission to access this resource"
                );

        response.setStatus(
                HttpServletResponse.SC_FORBIDDEN);

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