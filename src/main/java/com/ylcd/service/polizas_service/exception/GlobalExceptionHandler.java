package com.ylcd.service.polizas_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PolizaException.class)
    public ResponseEntity<ErrorResponse> handlePolizaException(PolizaException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Poliza Error", ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // Manejo unificado para errores de JSON malformado
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            org.springframework.web.server.ServerWebInputException.class
    })
    public ResponseEntity<ErrorResponse> handleJsonParseErrors(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Malformed JSON request",
                "El formato del JSON enviado no es válido",
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    // Handler genérico para cualquier otra excepción no manejada específicamente
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Server Error",
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}