package com.jsalvar.barbilling.controller.advise;

import com.jsalvar.barbilling.dto.response.ErrorResponseDto;
import com.jsalvar.barbilling.exception.UnprocessableEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {
    Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<ErrorResponseDto> buildResponseEntity(HttpStatus status, Exception ex) {
        logger.error("Exception handled: {}", ex.getMessage());

        ErrorResponseDto body = new ErrorResponseDto(
                status.getReasonPhrase(),
                ex.getMessage(),
                LocalDateTime.now().toString(),
                status.value()
        );
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<ErrorResponseDto> handleUnprocessableEntity(UnprocessableEntityException ex) {
        return buildResponseEntity(HttpStatus.UNPROCESSABLE_ENTITY, ex);
    }
}
