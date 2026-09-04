package com.eci.secureproductchallenge.dto;

import com.eci.secureproductchallenge.model.DeviceStatus;
import com.eci.secureproductchallenge.model.DeviceType;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record DeviceResponse(
        UUID id,
        String name,
        DeviceType type,
        String ipAddress,
        String vendor,
        DeviceStatus status,
        Instant lastConfigChange,
        String managedBy
) {
}
