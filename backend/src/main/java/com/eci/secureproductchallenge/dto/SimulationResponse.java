package com.eci.secureproductchallenge.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record SimulationResponse(
        UUID id,
        UUID deviceId,
        String deviceName,
        String scriptName,
        Map<String, String> parameters,
        boolean success,
        String output,
        Instant executedAt
) {
}
