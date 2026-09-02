package com.eci.secureproductchallenge.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Inventario FICTICIO de dispositivos de red (Laboratorio 3 - FDSI).
 * No representa infraestructura real.
 */
@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeviceType type;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(nullable = false, length = 100)
    private String vendor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeviceStatus status;

    @Column(name = "last_config_change", nullable = false)
    private Instant lastConfigChange;

    @Column(name = "managed_by", nullable = false, length = 150)
    private String managedBy;

    protected Device() {
        // JPA
    }

    public Device(String name, DeviceType type, String ipAddress, String vendor,
                  DeviceStatus status, Instant lastConfigChange, String managedBy) {
        this.name = name;
        this.type = type;
        this.ipAddress = ipAddress;
        this.vendor = vendor;
        this.status = status;
        this.lastConfigChange = lastConfigChange;
        this.managedBy = managedBy;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DeviceType getType() {
        return type;
    }

    public void setType(DeviceType type) {
        this.type = type;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }

    public Instant getLastConfigChange() {
        return lastConfigChange;
    }

    public void setLastConfigChange(Instant lastConfigChange) {
        this.lastConfigChange = lastConfigChange;
    }

    public String getManagedBy() {
        return managedBy;
    }

    public void setManagedBy(String managedBy) {
        this.managedBy = managedBy;
    }
}
