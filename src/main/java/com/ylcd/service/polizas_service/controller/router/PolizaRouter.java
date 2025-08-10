package com.ylcd.service.polizas_service.controller.router;

import com.ylcd.service.polizas_service.controller.handler.PolizaHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;

@Configuration
@RequiredArgsConstructor
public class PolizaRouter {

    private final PolizaHandler handler;

    @Bean
    public RouterFunction<?> rutasPoliza() {
        return RouterFunctions
                .route(RequestPredicates.GET("/api/polizas/{id}"), handler::obtenerPorId)
                .andRoute(RequestPredicates.GET("/api/polizas"), handler::obtenerPorDni)
                .andRoute(RequestPredicates.POST("/api/polizas"), handler::crear);
    }
}