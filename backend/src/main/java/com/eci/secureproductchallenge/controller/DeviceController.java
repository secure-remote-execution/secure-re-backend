package com.eci.secureproductchallenge.controller;

import com.eci.secureproductchallenge.dto.DeviceResponse;
import com.eci.secureproductchallenge.model.DeviceType;
import com.eci.secureproductchallenge.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Inventario FICTICIO de dispositivos de red. Solo lectura.
 *
 * // TODO: Lab 4 - agregar Spring Security + JWT + roles aquí.
 */
@RestController
@RequestMapping("/api/devices")
@Tag(name = "Devices", description = "Inventario ficticio de dispositivos de red (solo lectura)")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    @Operation(summary = "Lista el inventario de dispositivos, con filtro opcional por tipo")
    public List<DeviceResponse> getDevices(
            @Parameter(description = "Filtrar por tipo de dispositivo: FIREWALL, ROUTER o SWITCH")
            @RequestParam(required = false) DeviceType type) {
        return deviceService.getDevices(type);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene el detalle de un dispositivo por id")
    public DeviceResponse getDeviceById(@PathVariable UUID id) {
        return deviceService.getDeviceById(id);
    }
}
