package com.eci.secureproductchallenge.controller;

import com.eci.secureproductchallenge.dto.ScriptSimulationRequest;
import com.eci.secureproductchallenge.dto.ScriptSimulationResponse;
import com.eci.secureproductchallenge.service.ScriptSimulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Lab 4: requiere autenticación (Spring Security + JWT). Simular scripts es ADMIN-only.
@RestController
@RequestMapping("/api/scripts")
@RequiredArgsConstructor
@Tag(name = "Script Simulation", description = "Simulación de ejecución de scripts sobre dispositivos (no realiza cambios reales)")
public class ScriptSimulationController {

    private final ScriptSimulationService simulationService;

    @PostMapping("/simulate")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Simula la ejecución de un script sobre un dispositivo (no ejecuta nada real). Requiere rol ADMIN.")
    public ScriptSimulationResponse simulate(@Valid @RequestBody ScriptSimulationRequest request) {
        return simulationService.simulate(request);
    }

    @GetMapping("/simulate/history")
    @Operation(summary = "Historial de simulaciones registradas (evidencia para Blue Team)")
    public List<ScriptSimulationResponse> history() {
        return simulationService.history();
    }
}
