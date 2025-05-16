package com.vb.fundraiser.exception;

import com.vb.fundraiser.exception.box.BoxAlreadyAssignedException;
import com.vb.fundraiser.exception.box.BoxNotAssignedException;
import com.vb.fundraiser.exception.box.BoxNotFoundException;
import com.vb.fundraiser.exception.box.NotEmptyBoxAssignmentException;
import com.vb.fundraiser.exception.currency.CurrencyNotFoundException;
import com.vb.fundraiser.exception.currency.InvalidMoneyAmountException;
import com.vb.fundraiser.exception.event.FundraisingEventNotFoundException;
import com.vb.fundraiser.model.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    // 404 – Not Found
    @ExceptionHandler({
            BoxNotFoundException.class,
            FundraisingEventNotFoundException.class,
            CurrencyNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 400 – Business Logic Errors
    @ExceptionHandler({
            BoxAlreadyAssignedException.class,
            BoxNotAssignedException.class,
            NotEmptyBoxAssignmentException.class,
            InvalidMoneyAmountException.class,
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .findFirst()
                .orElse("Invalid request");

        return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    // 500 - Fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralError(Exception ex) {
        log.error("Unhandled exception", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred");
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message) {
        ErrorResponse body = new ErrorResponse(LocalDateTime.now(), status.value(), message);
        return new ResponseEntity<>(body, status);
    }
}