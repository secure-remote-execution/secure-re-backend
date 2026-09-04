package com.eci.secureproductchallenge.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record ScriptSimulationResponse(
        UUID id,
        UUID deviceId,
        String scriptName,
        String parameters,
        boolean success,
        String output,
        Instant requestedAt
) {
}
