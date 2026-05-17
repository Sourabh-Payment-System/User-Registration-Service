package payment.system.app.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import payment.system.app.dto.ErrorMessageDto;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle invalid username/password
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorMessageDto>
    handleBadCredentialsException(
            BadCredentialsException ex) {

        logger.error(
                "BadCredentialsException occurred : {}",
                ex.getMessage(),
                ex);

        ErrorMessageDto error =
                new ErrorMessageDto(
                        LocalDateTime.now(),
                        HttpStatus.UNAUTHORIZED.value(),
                        "Unauthorized",
                        "Invalid username or password");

        return new ResponseEntity<>(
                error,
                HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle user not found
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorMessageDto>
    handleUsernameNotFoundException(
            UsernameNotFoundException ex) {

        logger.error(
                "UsernameNotFoundException occurred : {}",
                ex.getMessage(),
                ex);

        ErrorMessageDto error =
                new ErrorMessageDto(
                        LocalDateTime.now(),
                        HttpStatus.NOT_FOUND.value(),
                        "User Not Found",
                        ex.getMessage());

        return new ResponseEntity<>(
                error,
                HttpStatus.NOT_FOUND);
    }

    /**
     * Handle access denied exception
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorMessageDto>
    handleAccessDeniedException(
            AccessDeniedException ex) {

        logger.error(
                "AccessDeniedException occurred : {}",
                ex.getMessage(),
                ex);

        ErrorMessageDto error =
                new ErrorMessageDto(
                        LocalDateTime.now(),
                        HttpStatus.FORBIDDEN.value(),
                        "Forbidden",
                        "You are not authorized to access this resource");

        return new ResponseEntity<>(
                error,
                HttpStatus.FORBIDDEN);
    }

    /**
     * Handle validation exceptions
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessageDto>
    handleValidationException(
            MethodArgumentNotValidException ex) {

        logger.error(
                "MethodArgumentNotValidException occurred : {}",
                ex.getMessage(),
                ex);

        Map<String, String> validationErrors =
                new HashMap<>();

        ex.getBindingResult()
          .getFieldErrors()
          .forEach(error ->
                  validationErrors.put(
                          error.getField(),
                          error.getDefaultMessage()));

        ErrorMessageDto error =
                new ErrorMessageDto(
                        LocalDateTime.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation Failed",
                        "Input validation failed",
                        validationErrors);

        return new ResponseEntity<>(
                error,
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle illegal argument exception
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorMessageDto>
    handleIllegalArgumentException(
            IllegalArgumentException ex) {

        logger.error(
                "IllegalArgumentException occurred : {}",
                ex.getMessage(),
                ex);

        ErrorMessageDto error =
                new ErrorMessageDto(
                        LocalDateTime.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        "Bad Request",
                        ex.getMessage());

        return new ResponseEntity<>(
                error,
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle all uncaught exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessageDto>
    handleGlobalException(
            Exception ex) {

        logger.error(
                "Unhandled exception occurred : {}",
                ex.getMessage(),
                ex);

        ErrorMessageDto error =
                new ErrorMessageDto(
                        LocalDateTime.now(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        "Something went wrong. Please try again later.");

        return new ResponseEntity<>(
                error,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}