package com.ylcd.service.polizas_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ylcd.service.polizas_service.model.request.PolizasRequest;
import com.ylcd.service.polizas_service.service.impl.PolizaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDate;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@WebFluxTest(PolizaController.class)
class PolizaControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private PolizaServiceImpl polizaService;

    private ObjectMapper objectMapper;
    private PolizasRequest polizaRequest;
    private PolizasRequest polizaResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        polizaRequest = new PolizasRequest(
                null,
                "VIDA",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                "12345678A"
        );

        polizaResponse = new PolizasRequest(
                "POL-001",
                "VIDA",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                "12345678A"
        );
    }

    @Test
    void obtenerPoliza_DeberiaRetornar200_CuandoPolizaExiste() {
        String polizaId = "POL-001";
        when(polizaService.obtenerPorId(polizaId))
                .thenReturn(Mono.just(polizaResponse));
        webTestClient.get()
                .uri("/api/polizas/{id}", polizaId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.code").isEqualTo("200")
                .jsonPath("$.status").isEqualTo(200)
                .jsonPath("$.comment").isEqualTo("Póliza encontrada")
                .jsonPath("$.data.id").isEqualTo("POL-001")
                .jsonPath("$.data.tipoSeguro").isEqualTo("VIDA")
                .jsonPath("$.data.dniCliente").isEqualTo("12345678A");
    }

    @Test
    void obtenerPoliza_DeberiaRetornar404_CuandoPolizaNoExiste() {

        String polizaId = "POL-999";
        when(polizaService.obtenerPorId(polizaId))
                .thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/polizas/{id}", polizaId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.code").isEqualTo("404")
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.comment").isEqualTo("Póliza no encontrada")
                .jsonPath("$.data").doesNotExist();
    }

    @Test
    void obtenerPoliza_DeberiaRetornar500_CuandoOcurreError() {
        String polizaId = "POL-001";
        when(polizaService.obtenerPorId(polizaId))
                .thenReturn(Mono.error(new RuntimeException("Error de base de datos")));

        webTestClient.get()
                .uri("/api/polizas/{id}", polizaId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.code").isEqualTo("500")
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.comment").isEqualTo("Error interno del servidor")
                .jsonPath("$.data").isEqualTo("Error de base de datos");
    }

    @Test
    void obtenerPorDni_DeberiaRetornarListaPolizas_CuandoDniExiste() {

        String dni = "12345678";
        PolizasRequest poliza1 = new PolizasRequest("POL-001", "VIDA",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), dni);
        PolizasRequest poliza2 = new PolizasRequest("POL-002", "AUTO",
                LocalDate.of(2024, 2, 1), LocalDate.of(2025, 1, 31), dni);

        when(polizaService.obtenerPorDni(dni))
                .thenReturn(Flux.just(poliza1, poliza2));

        webTestClient.get()
                .uri("/api/polizas?dni={dni}", dni)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$").value(hasSize(2))
                .jsonPath("$[0].code").isEqualTo("200")
                .jsonPath("$[0].comment").isEqualTo("Póliza obtenida correctamente")
                .jsonPath("$[0].data.id").isEqualTo("POL-001")
                .jsonPath("$[0].data.tipoSeguro").isEqualTo("VIDA")
                .jsonPath("$[0].data.dniCliente").isEqualTo(dni)
                .jsonPath("$[1].code").isEqualTo("200")
                .jsonPath("$[1].comment").isEqualTo("Póliza obtenida correctamente")
                .jsonPath("$[1].data.id").isEqualTo("POL-002")
                .jsonPath("$[1].data.tipoSeguro").isEqualTo("AUTO")
                .jsonPath("$[1].data.dniCliente").isEqualTo(dni);
    }
    @Test
    void obtenerPorDni_DeberiaRetornarListaVacia_CuandoDniNoTienePolizas() {

        String dni = "87654321B";
        when(polizaService.obtenerPorDni(dni))
                .thenReturn(Flux.empty());


        webTestClient.get()
                .uri("/api/polizas?dni={dni}", dni)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].code").isEqualTo("404")
                .jsonPath("$[0].status").isEqualTo(null)
                .jsonPath("$[0].comment").isEqualTo("No se encontraron pólizas para el DNI: "+dni)
                .jsonPath("$[0].data").isEqualTo(null);
    }


    @Test
    void crear_DeberiaRetornar201_CuandoPolizaEsCreadaExitosamente() throws Exception {

        when(polizaService.crearPoliza(any(PolizasRequest.class)))
                .thenReturn(Mono.just(polizaResponse));

        webTestClient.post()
                .uri("/api/polizas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(polizaRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.code").isEqualTo("201")
                .jsonPath("$.status").isEqualTo(201)
                .jsonPath("$.comment").isEqualTo("Póliza creada con éxito")
                .jsonPath("$.data.id").isEqualTo("POL-001")
                .jsonPath("$.data.tipoSeguro").isEqualTo("VIDA")
                .jsonPath("$.data.dniCliente").isEqualTo("12345678A");
    }

    @Test
    void crear_DeberiaRetornar500_CuandoOcurreErrorAlCrear() throws Exception {

        when(polizaService.crearPoliza(any(PolizasRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("Error al guardar en BD")));


        webTestClient.post()
                .uri("/api/polizas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(polizaRequest)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.code").isEqualTo("500")
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.comment").isEqualTo("Error interno del servidor")
                .jsonPath("$.data").isEqualTo("Error al guardar en BD");
    }

    @Test
    void crear_DeberiaRetornar400_CuandoBodyEstaVacio() {

        webTestClient.post()
                .uri("/api/polizas")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Malformed JSON request")
                .jsonPath("$.message").isEqualTo("El formato del JSON enviado no es válido")
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void crear_DeberiaRetornar400_CuandoJsonMalFormado() {
        String invalidJson = "{ \"campo\": \"valor\" ";

        webTestClient.post()
                .uri("/api/polizas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidJson)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Malformed JSON request")
                .jsonPath("$.message").isEqualTo("El formato del JSON enviado no es válido")
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void crear_DeberiaAceptarPolizaConFechasValidas() throws Exception {

        PolizasRequest polizaFechasFuturas = new PolizasRequest(
                null,
                "HOGAR",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusYears(1),
                "11111111A"
        );

        PolizasRequest polizaRespuestaFuturas = new PolizasRequest(
                "POL-003",
                "HOGAR",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusYears(1),
                "11111111A"
        );

        when(polizaService.crearPoliza(any(PolizasRequest.class)))
                .thenReturn(Mono.just(polizaRespuestaFuturas));


        webTestClient.post()
                .uri("/api/polizas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(polizaFechasFuturas)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.code").isEqualTo("201")
                .jsonPath("$.data.tipoSeguro").isEqualTo("HOGAR");
    }
}