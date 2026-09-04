package com.eci.secureproductchallenge.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Registro de una simulación de ejecución de script.
 * IMPORTANTE: nunca representa un cambio real sobre un dispositivo.
 * Solo queda como evidencia para el Blue Team.
 */
@Entity
@Table(name = "script_simulations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScriptSimulation {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "device_id", nullable = false)
    private UUID deviceId;

    @Column(name = "script_name", nullable = false, length = 100)
    private String scriptName;

    @Column(length = 500)
    private String parameters;

    @Column(nullable = false)
    private boolean success;

    @Column(nullable = false, length = 300)
    private String output;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;
}
