package com.eci.secureproductchallenge.controller;

import com.eci.secureproductchallenge.dto.SimulationRequest;
import com.eci.secureproductchallenge.dto.SimulationResponse;
import com.eci.secureproductchallenge.service.ScriptSimulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Simulación de ejecución de scripts sobre dispositivos del inventario.
 * IMPORTANTE: nunca ejecuta nada real; solo queda registrado como evidencia
 * para el Blue Team (ver filter/RequestLoggingFilter y access.log de Nginx).
 *
 * // TODO: Lab 4 - agregar Spring Security + JWT + roles aquí.
 */
@RestController
@RequestMapping("/api/scripts")
@Tag(name = "Script Simulation", description = "Simulación (NO ejecución real) de scripts sobre dispositivos")
public class ScriptSimulationController {

    private final ScriptSimulationService scriptSimulationService;

    public ScriptSimulationController(ScriptSimulationService scriptSimulationService) {
        this.scriptSimulationService = scriptSimulationService;
    }

    @PostMapping("/simulate")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Simula la ejecución de un script sobre un dispositivo (no aplica cambios reales)")
    public SimulationResponse simulate(@Valid @RequestBody SimulationRequest request, HttpServletRequest httpRequest) {
        return scriptSimulationService.simulate(request, resolveClientIp(httpRequest));
    }

    @GetMapping("/simulate/history")
    @Operation(summary = "Lista el historial de simulaciones registradas, como evidencia de lo intentado")
    public List<SimulationResponse> getHistory() {
        return scriptSimulationService.getHistory();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
