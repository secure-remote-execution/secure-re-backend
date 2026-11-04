package com.eci.secureproductchallenge.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record LoginResponse(
        String token,
        String username,
        String role,
        Instant expiresAt
) {
}
