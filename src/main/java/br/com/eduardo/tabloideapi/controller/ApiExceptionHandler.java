package br.com.eduardo.tabloideapi.controller;

import br.com.eduardo.tabloideapi.dto.ApiError;
import br.com.eduardo.tabloideapi.exception.ProcessingBusyException;
import br.com.eduardo.tabloideapi.exception.TabloideExtractionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleInvalidInput(IllegalArgumentException exception) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(TabloideExtractionException.class)
    public ResponseEntity<ApiError> handleExtractionFailure(TabloideExtractionException exception) {
        return response(HttpStatus.BAD_GATEWAY, exception.getMessage());
    }

    @ExceptionHandler(ProcessingBusyException.class)
    public ResponseEntity<ApiError> handleProcessingBusy(ProcessingBusyException exception) {
        return response(HttpStatus.TOO_MANY_REQUESTS, exception.getMessage());
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message
        ));
    }
}
