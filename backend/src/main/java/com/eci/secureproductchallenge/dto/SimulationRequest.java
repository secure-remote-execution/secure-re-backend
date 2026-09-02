package com.eci.secureproductchallenge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * Solicitud de SIMULACIÓN de ejecución de script. No dispara ninguna acción
 * real sobre el dispositivo: solo queda registrada como evidencia.
 */
public record SimulationRequest(
        @NotNull(message = "deviceId es obligatorio")
        UUID deviceId,

        @NotBlank(message = "scriptName es obligatorio")
        String scriptName,

        Map<String, String> parameters
) {
}
