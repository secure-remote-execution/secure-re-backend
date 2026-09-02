package com.eci.secureproductchallenge.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Evidencia de una SIMULACIÓN de ejecución de script sobre un dispositivo.
 * Nunca representa un cambio real aplicado sobre infraestructura.
 */
@Entity
@Table(name = "simulation_records")
public class SimulationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "device_name", nullable = false, length = 100)
    private String deviceName;

    @Column(name = "script_name", nullable = false, length = 150)
    private String scriptName;

    @Column(name = "parameters", columnDefinition = "TEXT")
    private String parametersJson;

    @Column(nullable = false)
    private boolean success;

    @Column(nullable = false, length = 500)
    private String output;

    @Column(name = "source_ip", length = 45)
    private String sourceIp;

    @Column(name = "executed_at", nullable = false)
    private Instant executedAt;

    protected SimulationRecord() {
        // JPA
    }

    public SimulationRecord(Device device, String deviceName, String scriptName, String parametersJson,
                             boolean success, String output, String sourceIp, Instant executedAt) {
        this.device = device;
        this.deviceName = deviceName;
        this.scriptName = scriptName;
        this.parametersJson = parametersJson;
        this.success = success;
        this.output = output;
        this.sourceIp = sourceIp;
        this.executedAt = executedAt;
    }

    public UUID getId() {
        return id;
    }

    public Device getDevice() {
        return device;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getScriptName() {
        return scriptName;
    }

    public String getParametersJson() {
        return parametersJson;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getOutput() {
        return output;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }
}
