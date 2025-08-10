package com.ylcd.service.polizas_service.exception;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor

public class PolizaException extends RuntimeException {

    private String message;

    public PolizaException(String message) {
        super(message);
        this.message = message;
    }


}
