package com.jsalvar.barbilling.controller.advise;

import com.jsalvar.barbilling.dto.response.ErrorResponseDto;
import com.jsalvar.barbilling.exception.ResourceNotFoundException;
import com.jsalvar.barbilling.exception.UnprocessableEntityException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<ErrorResponseDto> buildResponseEntity(HttpStatus status, Exception ex) {
        return buildResponseEntity(status, ex, ex.getMessage());
    }

    private ResponseEntity<ErrorResponseDto> buildResponseEntity(HttpStatus status, Exception ex, String customMessage) {
        logger.error("Exception handled: {}", ex.getMessage());

        ErrorResponseDto body = new ErrorResponseDto(
                status.getReasonPhrase(),
                customMessage,
                LocalDateTime.now().toString(),
                status.value()
        );
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<ErrorResponseDto> handleUnprocessableEntity(UnprocessableEntityException ex) {
        return buildResponseEntity(HttpStatus.UNPROCESSABLE_ENTITY, ex);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleBadCredentials(BadCredentialsException ex) {
        return buildResponseEntity(HttpStatus.UNAUTHORIZED, ex, "Invalid credentials");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponseEntity(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUsernameNotFound(UsernameNotFoundException ex) {
        return buildResponseEntity(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleEntityNotFound(EntityNotFoundException ex) {
        return buildResponseEntity(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFound(ResourceNotFoundException ex) {
        return buildResponseEntity(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex, message);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return buildResponseEntity(HttpStatus.CONFLICT, ex, "A record with this value already exists");
    }


}
