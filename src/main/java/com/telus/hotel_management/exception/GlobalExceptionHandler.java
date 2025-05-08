package com.telus.hotel_management.exception;

import com.telus.hotel_management.entity.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(404).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({UserAlreadyExistsException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(Exception ex) {
        return ResponseEntity.status(400).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> accessDeniedException(Exception ex) {
        return ResponseEntity.status(403).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({InvalidRoleException.class, UnauthorizedException.class})
    public ResponseEntity<ErrorResponse> unauthorizedException(Exception ex) {
        return ResponseEntity.status(401).body(new ErrorResponse(ex.getMessage()));
    }
    // create a handler for invalid login credentials

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
        log.info("Handling global exception");
        log.error("Global exception caught", ex);

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");

        if (ex != null) {
            body.put("message", ex.getMessage());
        } else {
            body.put("message", "Unknown error");
        }

        if (request != null) {
            body.put("path", request.getDescription(false));
        } else {
            body.put("path", "Unknown path");
        }

        log.debug("Returning response for global exception: {}", body);
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
