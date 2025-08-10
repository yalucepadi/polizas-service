package com.ylcd.service.polizas_service.controller.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ylcd.service.polizas_service.exception.ErrorResponse;
import com.ylcd.service.polizas_service.model.request.PolizasRequest;
import com.ylcd.service.polizas_service.model.response.ResponseGeneralDto;
import com.ylcd.service.polizas_service.service.impl.PolizaServiceImpl;
import com.ylcd.service.polizas_service.util.Constants;
import com.ylcd.service.polizas_service.util.PolizaAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PolizaHandler {

    private final PolizaServiceImpl service;

    public Mono<ServerResponse> obtenerPorId(ServerRequest request) {
        String id = request.pathVariable("id");
        log.debug("Buscando póliza por ID: {}", id);

        return service.obtenerPorId(id)
                .flatMap(poliza -> {
                    log.info("Póliza encontrada: {}", poliza);
                    return ServerResponse.ok()
                            .bodyValue(PolizaAdapter.responseGeneral(
                                    Constants.HTTP_200, Constants.HTTP_200_code,
                                    "Póliza encontrada", poliza
                            ));
                })
                .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND)
                        .bodyValue(PolizaAdapter.responseGeneral(
                                Constants.HTTP_404, Constants.HTTP_404_code,
                                "Póliza no encontrada", null
                        )))
                .onErrorResume(error -> {
                    log.error("Error al buscar póliza {}: {}", id, error.getMessage(), error);
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .bodyValue(PolizaAdapter.responseGeneral(
                                    Constants.HTTP_500, Constants.HTTP_500_code,
                                    "Error interno del servidor", error.getMessage()
                            ));
                });
    }

    public Mono<ServerResponse> obtenerPorDni(ServerRequest request) {
        String dni = request.queryParam("dni").orElse(null);
        if (dni == null) {
            return ServerResponse.badRequest()
                    .bodyValue(ResponseGeneralDto.builder()
                            .code(Constants.HTTP_400)
                            .status(Constants.HTTP_400_code)
                            .comment("dni es requerido")
                            .data(null)
                            .build());
        }

        log.debug("Listando pólizas por DNI: {}", dni);

        Flux<ResponseGeneralDto> flujo = service.obtenerPorDni(dni)
                .map(poliza -> ResponseGeneralDto.<PolizasRequest>builder()
                        .code(Constants.HTTP_200)
                        .status(Constants.HTTP_200_code)
                        .comment("Póliza obtenida correctamente")
                        .data(poliza)
                        .build())
                .switchIfEmpty(Flux.just(ResponseGeneralDto.<PolizasRequest>builder()
                        .code(Constants.HTTP_404)
                        .status(Constants.HTTP_404_code)
                        .comment("No se encontraron pólizas para el DNI: " + dni)
                        .data(null)
                        .build()))
                .onErrorResume(e -> Flux.just(ResponseGeneralDto.<PolizasRequest>builder()
                        .code(Constants.HTTP_500)
                        .status(Constants.HTTP_500_code)
                        .comment("Error interno del servidor: " + e.getMessage())
                        .data(null)
                        .build()));

        return ServerResponse.ok().body(flujo, ResponseGeneralDto.class);
    }

    public Mono<ServerResponse> crear(ServerRequest request) {
        return request.bodyToMono(PolizasRequest.class)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Body vacío")))
                .flatMap(poliza -> {
                    log.debug("Creando póliza: {}", poliza);
                    return service.crearPoliza(poliza)
                            .flatMap(creada -> {
                                log.info("Póliza creada: {}", creada);
                                return ServerResponse.status(HttpStatus.CREATED)
                                        .bodyValue(PolizaAdapter.responseGeneral(
                                                Constants.HTTP_201,
                                                Constants.HTTP_201_code,
                                                "Póliza creada con éxito",
                                                creada
                                        ));
                            })
                            .onErrorResume(error -> {
                                log.error("Error creando póliza: {}", error.getMessage(), error);
                                return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .bodyValue(PolizaAdapter.responseGeneral(
                                                Constants.HTTP_500,
                                                Constants.HTTP_500_code,
                                                "Error interno del servidor",
                                                error.getMessage()
                                        ));
                            });
                })
                .onErrorResume(IllegalArgumentException.class, error -> {
                    log.warn("Error en request: {}", error.getMessage());
                    return ServerResponse.status(HttpStatus.BAD_REQUEST)
                            .bodyValue(new ErrorResponse(
                                    "Malformed JSON request",
                                    "El formato del JSON enviado no es válido",
                                    400
                            ));
                })
                .onErrorResume(e -> {
                    log.warn("Error de parseo JSON: {}", e.getMessage());
                    return ServerResponse.status(HttpStatus.BAD_REQUEST)
                            .bodyValue(new ErrorResponse(
                                    "Malformed JSON request",
                                    "El formato del JSON enviado no es válido",
                                    400
                            ));
                });
    }
}