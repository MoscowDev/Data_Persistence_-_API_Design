package com.mxr.integration.exceptions;

import com.mxr.integration.Response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PersonNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePersonNotFoundException(PersonNotFoundException ex) {
        return new ResponseEntity<>(
                new ErrorResponse("error", ex.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        String message = ex.getMessage() == null ? "Invalid query parameters" : ex.getMessage();
        if ("uninterpretable q".equals(message)) {
            message = "Unable to interpret query";
        } else if ("invalid sort_by".equals(message) || "invalid page".equals(message)) {
            message = "Invalid query parameters";
        }
        
        return new ResponseEntity<>(
                new ErrorResponse("error", message),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        return new ResponseEntity<>(
                new ErrorResponse("error", "Invalid query parameters"),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        return new ResponseEntity<>(
                new ErrorResponse("error", ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}