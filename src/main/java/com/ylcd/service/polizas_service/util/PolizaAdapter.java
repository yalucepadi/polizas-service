package com.ylcd.service.polizas_service.util;


import com.ylcd.service.polizas_service.model.response.ResponseGeneralDto;

public class PolizaAdapter {
    public static ResponseGeneralDto responseGeneral(String code, Integer status, String message, Object data) {
        return ResponseGeneralDto.builder()
                .status(status)
                .code(code)
                .comment(message)
                .data(data)
                .build();



    }}
