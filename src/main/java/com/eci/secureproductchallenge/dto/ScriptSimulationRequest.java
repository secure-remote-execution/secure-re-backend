package com.eci.secureproductchallenge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ScriptSimulationRequest(
        @NotNull(message = "deviceId es obligatorio")
        UUID deviceId,

        @NotBlank(message = "scriptName es obligatorio")
        String scriptName,

        String parameters
) {
}
