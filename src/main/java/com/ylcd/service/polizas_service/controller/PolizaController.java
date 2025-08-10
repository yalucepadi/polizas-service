package com.ylcd.service.polizas_service.controller;

import com.ylcd.service.polizas_service.model.request.PolizasRequest;
import com.ylcd.service.polizas_service.model.response.ResponseGeneralDto;
import com.ylcd.service.polizas_service.service.impl.PolizaServiceImpl;
import com.ylcd.service.polizas_service.util.PolizaAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("api/polizas")
public class PolizaController {

    private final PolizaServiceImpl service;

    public PolizaController(PolizaServiceImpl service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ResponseGeneralDto>> obtenerPoliza(@PathVariable String id) {
        log.debug("Buscando póliza por ID: {}", id);

        return service.obtenerPorId(id)
                .map(poliza -> {
                    log.info("Póliza encontrada: {}", poliza);
                    return ResponseEntity.ok(
                            PolizaAdapter.responseGeneral(
                                    "200", HttpStatus.OK.value(), "Póliza encontrada", poliza
                            )
                    );
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(PolizaAdapter.responseGeneral(
                                "404", HttpStatus.NOT_FOUND.value(), "Póliza no encontrada", null
                        )))
                .onErrorResume(error -> {
                    log.error("Error al buscar póliza {}: {}", id, error.getMessage(), error);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(PolizaAdapter.responseGeneral(
                                    "500", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                    "Error interno del servidor", error.getMessage()
                            )));
                });
    }

    @GetMapping
    public Flux<ResponseGeneralDto> obtenerPorDni(@RequestParam String dni) {
        log.debug("Listando pólizas por DNI: {}", dni);

        return service.obtenerPorDni(dni)
                .map(poliza -> ResponseGeneralDto.<PolizasRequest>builder()
                        .code("200")
                        .comment("Póliza obtenida correctamente")
                        .data(poliza)
                        .build()
                )
                .switchIfEmpty(Flux.just(ResponseGeneralDto.<PolizasRequest>builder()
                        .code("404")
                        .comment("No se encontraron pólizas para el DNI: " + dni)
                        .data(null)
                        .build()))
                .onErrorResume(e -> Flux.just(ResponseGeneralDto.<PolizasRequest>builder()
                        .code("500")
                        .comment("Error interno del servidor: " + e.getMessage())
                        .data(null)
                        .build()));
    }

    @PostMapping
    public Mono<ResponseEntity<ResponseGeneralDto>> crear(@RequestBody PolizasRequest poliza) {
        log.debug("Creando póliza: {}", poliza);

        return service.crearPoliza(poliza)
                .map(creada -> {
                    log.info("Póliza creada: {}", creada);
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .body(PolizaAdapter.responseGeneral(
                                    "201", HttpStatus.CREATED.value(), "Póliza creada con éxito", creada
                            ));
                })
                .onErrorResume(error -> {
                    log.error("Error creando póliza: {}", error.getMessage(), error);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(PolizaAdapter.responseGeneral(
                                    "500", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                    "Error interno del servidor", error.getMessage()
                            )));
                });
    }
}