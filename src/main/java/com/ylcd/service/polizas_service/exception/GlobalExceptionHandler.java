package com.ylcd.service.polizas_service.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        HttpStatus status;
        Object body;

        if (ex instanceof PolizaException) {
            status = HttpStatus.BAD_REQUEST;
            body = new ErrorResponse("Poliza Error", ex.getMessage(), status.value());
        }
        else if (ex instanceof org.springframework.web.server.ServerWebInputException) {
            status = HttpStatus.BAD_REQUEST;
            body = new ErrorResponse("Malformed JSON request", "El formato del JSON enviado no es válido", status.value());
        }
        else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            body = new ErrorResponse("Server Error", ex.getMessage(), status.value());
        }

        exchange.getResponse().setStatusCode(status);

        DataBuffer buffer;
        try {
            buffer = exchange.getResponse()
                    .bufferFactory()
                    .wrap(objectMapper.writeValueAsBytes(body));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}